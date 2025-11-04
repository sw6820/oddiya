#!/bin/bash
# User data script for Oddiya Database Server (EC2 #2)

set -e

# Log everything
exec > >(tee /var/log/user-data.log)
exec 2>&1

echo "===== Starting Oddiya DB Server Setup ====="
echo "Date: $(date)"
echo "Hostname: $(hostname)"

# NOTE: This server has NO direct internet access (cost optimization)
# Package installation may fail - updates should be done via SSH tunnel

# Update system (may fail without NAT Gateway)
echo "Updating system packages..."
dnf update -y || echo "WARNING: dnf update failed - no internet access. Update manually via SSH."

# Install PostgreSQL 15 (17 not available in default repos yet)
echo "Installing PostgreSQL..."
dnf install -y postgresql15 postgresql15-server postgresql15-contrib

# Initialize PostgreSQL database
echo "Initializing PostgreSQL database..."
postgresql-setup --initdb

# Start PostgreSQL
echo "Starting PostgreSQL service..."
systemctl start postgresql
systemctl enable postgresql

# Wait for PostgreSQL to be ready
sleep 5

# Create backup directory
mkdir -p /opt/backups
chown postgres:postgres /opt/backups

# Create backup script
cat > /opt/backups/backup-db.sh <<'EOF'
#!/bin/bash
DATE=$(date +%Y-%m-%d-%H%M)
BACKUP_FILE="/opt/backups/oddiya-backup-$DATE.sql"

# Create backup
pg_dump -U postgres oddiya > $BACKUP_FILE

# Compress
gzip $BACKUP_FILE

# Keep only last 7 days
find /opt/backups -name "oddiya-backup-*.sql.gz" -mtime +7 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
EOF

chmod +x /opt/backups/backup-db.sh

# Install AWS CLI for S3 backups (optional)
echo "Installing AWS CLI..."
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
dnf install -y unzip
unzip awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip

# Enable SSM agent
echo "Enabling SSM agent..."
systemctl enable amazon-ssm-agent
systemctl start amazon-ssm-agent

# Create deployment marker
echo "Database server initialized at $(date)" > /opt/DB_INITIALIZED

echo "===== DB Server Setup Complete ====="
echo "Next steps:"
echo "1. Configure PostgreSQL (pg_hba.conf, postgresql.conf)"
echo "2. Create oddiya database and user"
echo "3. Run Flyway migrations"
echo "4. Set up cron job for backups"
