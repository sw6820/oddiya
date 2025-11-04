#!/bin/bash
# Oddiya Phase 1 Database Setup Script
# Sets up PostgreSQL on EC2 Database Server

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}===== Oddiya Database Setup =====${NC}"

# Get EC2 IPs
get_ec2_ips() {
    echo -e "${YELLOW}Getting EC2 IPs from Terraform...${NC}"

    cd infrastructure/terraform/phase1

    APP_SERVER_IP=$(terraform output -raw app_server_public_ip 2>/dev/null)
    DB_SERVER_IP=$(terraform output -raw db_server_private_ip 2>/dev/null)

    if [ -z "$APP_SERVER_IP" ] || [ -z "$DB_SERVER_IP" ]; then
        echo -e "${RED}Error: Could not get IPs. Run 'terraform apply' first${NC}"
        exit 1
    fi

    echo -e "${GREEN}App Server IP: $APP_SERVER_IP${NC}"
    echo -e "${GREEN}DB Server IP: $DB_SERVER_IP${NC}"

    cd ../../..
}

# Configure PostgreSQL
configure_postgresql() {
    echo -e "${YELLOW}Configuring PostgreSQL...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    # SSH to DB server via app server (jump host)
    cat << 'EOF_POSTGRES_CONFIG' | ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP ec2-user@$DB_SERVER_IP 'bash -s'

# Configure PostgreSQL to listen on private IP
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/g" \
    /var/lib/pgsql/data/postgresql.conf

# Set max connections (t2.micro constraint)
sudo sed -i "s/max_connections = 100/max_connections = 20/g" \
    /var/lib/pgsql/data/postgresql.conf

# Set shared buffers
sudo sed -i "s/#shared_buffers = 128MB/shared_buffers = 256MB/g" \
    /var/lib/pgsql/data/postgresql.conf

# Configure pg_hba.conf to allow app server connections
echo "# Allow app server" | sudo tee -a /var/lib/pgsql/data/pg_hba.conf
echo "host    oddiya    admin    APP_SERVER_IP/32    scram-sha-256" | sudo tee -a /var/lib/pgsql/data/pg_hba.conf

# Restart PostgreSQL
sudo systemctl restart postgresql

echo "PostgreSQL configured"
EOF_POSTGRES_CONFIG

    echo -e "${GREEN}✓ PostgreSQL configured${NC}"
}

# Create database and user
create_database() {
    echo -e "${YELLOW}Creating database and user...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    # Prompt for password
    read -sp "Enter database password for 'admin' user: " DB_PASSWORD
    echo ""
    read -sp "Confirm password: " DB_PASSWORD_CONFIRM
    echo ""

    if [ "$DB_PASSWORD" != "$DB_PASSWORD_CONFIRM" ]; then
        echo -e "${RED}Error: Passwords do not match${NC}"
        exit 1
    fi

    # Create database
    cat << EOF_CREATE_DB | ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP ec2-user@$DB_SERVER_IP 'bash -s' "$DB_PASSWORD"

DB_PASSWORD=\$1

sudo -u postgres psql << EOF
-- Create database
CREATE DATABASE oddiya;

-- Create user
CREATE USER admin WITH PASSWORD '\$DB_PASSWORD';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE oddiya TO admin;

-- Connect to oddiya database
\c oddiya

-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth_service;
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS plan_service;
CREATE SCHEMA IF NOT EXISTS video_service;

-- Grant schema privileges
GRANT ALL ON SCHEMA auth_service TO admin;
GRANT ALL ON SCHEMA user_service TO admin;
GRANT ALL ON SCHEMA plan_service TO admin;
GRANT ALL ON SCHEMA video_service TO admin;

-- Set default privileges
ALTER DEFAULT PRIVILEGES IN SCHEMA plan_service GRANT ALL ON TABLES TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA plan_service GRANT ALL ON SEQUENCES TO admin;

EOF

echo "Database created"
EOF_CREATE_DB

    echo -e "${GREEN}✓ Database and user created${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  IMPORTANT: Save this password securely!${NC}"
    echo "You will need it for Plan Service configuration"
}

# Run migrations
run_migrations() {
    echo -e "${YELLOW}Running database migrations...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    # Copy migration files to DB server
    echo "Copying migration files..."
    scp -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP \
        services/plan-service/src/main/resources/db/migration/*.sql \
        ec2-user@$DB_SERVER_IP:/tmp/

    # Run migrations
    cat << 'EOF_MIGRATIONS' | ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP ec2-user@$DB_SERVER_IP 'bash -s'

for file in /tmp/V*.sql; do
    echo "Running migration: $file"
    sudo -u postgres psql -d oddiya -f "$file"
done

echo "Migrations completed"
EOF_MIGRATIONS

    echo -e "${GREEN}✓ Migrations completed${NC}"
}

# Verify database
verify_database() {
    echo -e "${YELLOW}Verifying database setup...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    cat << 'EOF_VERIFY' | ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP ec2-user@$DB_SERVER_IP 'bash -s'

sudo -u postgres psql -d oddiya << EOF
-- List schemas
SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%_service';

-- List tables in plan_service
SELECT table_schema, table_name FROM information_schema.tables
WHERE table_schema = 'plan_service';

-- Count rows
SELECT 'travel_plans' as table, COUNT(*) as rows FROM plan_service.travel_plans
UNION ALL
SELECT 'plan_details', COUNT(*) FROM plan_service.plan_details;
EOF
EOF_VERIFY

    echo -e "${GREEN}✓ Database verification complete${NC}"
}

# Setup backup cron job
setup_backup() {
    echo -e "${YELLOW}Setting up backup cron job...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    cat << 'EOF_BACKUP' | ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no \
        -o ProxyJump=ec2-user@$APP_SERVER_IP ec2-user@$DB_SERVER_IP 'bash -s'

# Add backup cron job (daily at 2 AM)
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/backups/backup-db.sh") | crontab -

echo "Backup cron job added"
crontab -l
EOF_BACKUP

    echo -e "${GREEN}✓ Backup cron job configured${NC}"
}

# Test connection from app server
test_connection() {
    echo -e "${YELLOW}Testing database connection from app server...${NC}"

    KEY_PATH="$HOME/.ssh/oddiya-prod.pem"

    ssh -i "$KEY_PATH" -o StrictHostKeyChecking=no ec2-user@$APP_SERVER_IP << EOF_TEST
# Test connection
psql -h $DB_SERVER_IP -U admin -d oddiya -c "SELECT version();"
EOF_TEST

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Connection test successful${NC}"
    else
        echo -e "${RED}✗ Connection test failed${NC}"
        echo "Make sure to configure the 'admin' user password in app server"
    fi
}

# Print summary
print_summary() {
    echo ""
    echo -e "${GREEN}===== Database Setup Complete! =====${NC}"
    echo ""
    echo "📝 Configuration Summary:"
    echo ""
    echo "Database Name: oddiya"
    echo "User: admin"
    echo "Host: $DB_SERVER_IP"
    echo "Port: 5432"
    echo ""
    echo "Schemas:"
    echo "  - auth_service"
    echo "  - user_service"
    echo "  - plan_service"
    echo "  - video_service"
    echo ""
    echo "📝 Next Steps:"
    echo ""
    echo "1. Update Plan Service configuration:"
    echo "   ssh -i ~/.ssh/oddiya-prod.pem ec2-user@$APP_SERVER_IP"
    echo "   sudo nano /etc/systemd/system/plan-service.service"
    echo "   # Update DB_PASSWORD environment variable"
    echo ""
    echo "2. Restart Plan Service:"
    echo "   sudo systemctl daemon-reload"
    echo "   sudo systemctl restart plan-service"
    echo ""
    echo "3. Test end-to-end:"
    echo "   curl -X POST http://$APP_SERVER_IP:8083/api/v1/plans \\"
    echo "     -H 'Content-Type: application/json' \\"
    echo "     -H 'X-User-Id: 1' \\"
    echo "     -d '{\"destination\":\"Seoul\",\"startDate\":\"2025-11-10\",\"endDate\":\"2025-11-12\",\"budget\":100000}'"
    echo ""
    echo "4. Verify in database:"
    echo "   psql -h $DB_SERVER_IP -U admin -d oddiya -c 'SELECT * FROM plan_service.travel_plans;'"
    echo ""
}

# Main execution
main() {
    get_ec2_ips
    configure_postgresql
    create_database

    # Check if migration files exist
    if [ -d "services/plan-service/src/main/resources/db/migration" ]; then
        run_migrations
    else
        echo -e "${YELLOW}⚠️  No migration files found, skipping migrations${NC}"
    fi

    verify_database
    setup_backup
    test_connection
    print_summary
}

main
