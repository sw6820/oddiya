# Phase 1 Deployment Plan - 2x t2.micro EC2

**Version:** v1.4 - Phase 1 Production Deployment
**Date:** 2025-11-04
**Objective:** Deploy Java + Python services on one EC2, PostgreSQL on another

---

## 🎯 Deployment Architecture

### Infrastructure Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         AWS VPC                              │
│                     (10.0.0.0/16)                           │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │           Public Subnet (10.0.1.0/24)              │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────┐     │    │
│  │  │   EC2 #1: Application Server             │     │    │
│  │  │   Type: t2.micro (1 vCPU, 1GB RAM)      │     │    │
│  │  │   OS: Amazon Linux 2023                  │     │    │
│  │  │   Public IP: Yes (Elastic IP)            │     │    │
│  │  │                                           │     │    │
│  │  │   Services:                              │     │    │
│  │  │   - Java Plan Service (Port 8083)       │     │    │
│  │  │   - Python LLM Agent (Port 8000)        │     │    │
│  │  │                                           │     │    │
│  │  │   Security Group: allow 22, 8000, 8083  │     │    │
│  │  └──────────────────────────────────────────┘     │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │          Private Subnet (10.0.2.0/24)              │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────┐     │    │
│  │  │   EC2 #2: Database Server                │     │    │
│  │  │   Type: t2.micro (1 vCPU, 1GB RAM)      │     │    │
│  │  │   OS: Amazon Linux 2023                  │     │    │
│  │  │   Public IP: No (NAT Gateway for updates)│     │    │
│  │  │                                           │     │    │
│  │  │   Services:                              │     │    │
│  │  │   - PostgreSQL 17.0 (Port 5432)         │     │    │
│  │  │                                           │     │    │
│  │  │   Security Group: allow 5432 from EC2#1 │     │    │
│  │  └──────────────────────────────────────────┘     │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  Internet Gateway (for EC2 #1)                              │
│  NAT Gateway (for EC2 #2 updates)                          │
└─────────────────────────────────────────────────────────────┘

Mobile App → EC2 #1 (Public IP:8083, :8000) → EC2 #2 (Private IP:5432)
```

---

## 📊 Resource Specifications

### EC2 Instance #1: Application Server

| Specification | Value |
|---------------|-------|
| Instance Type | t2.micro |
| vCPU | 1 |
| Memory | 1 GB |
| Storage | 20 GB gp3 EBS |
| OS | Amazon Linux 2023 |
| Network | Public subnet, Elastic IP |
| Monthly Cost | $8.50 (after free tier) |

**Installed Software:**
- Java 21 (Amazon Corretto)
- Python 3.11
- Systemd services for both applications

**Services:**
1. **plan-service.service** (Java Spring Boot)
   - Port: 8083
   - Memory: 512 MB (JVM -Xmx512m)
   - Auto-restart: Yes

2. **llm-agent.service** (Python FastAPI)
   - Port: 8000
   - Workers: 1
   - Auto-restart: Yes

### EC2 Instance #2: Database Server

| Specification | Value |
|---------------|-------|
| Instance Type | t2.micro |
| vCPU | 1 |
| Memory | 1 GB |
| Storage | 30 GB gp3 EBS (optimized for database) |
| OS | Amazon Linux 2023 |
| Network | Private subnet, no public IP |
| Monthly Cost | $8.50 (after free tier) |

**Installed Software:**
- PostgreSQL 17.0
- Automated backups to S3 (daily)

**Database:**
- **Database Name:** oddiya
- **Schemas:** auth_service, user_service, plan_service, video_service
- **User:** admin (restricted to EC2 #1 IP only)
- **Max Connections:** 20 (t2.micro constraint)
- **Shared Buffers:** 256 MB

---

## 🔐 Security Configuration

### Security Group #1: Application Server (sg-app-server)

| Type | Protocol | Port | Source | Purpose |
|------|----------|------|--------|---------|
| SSH | TCP | 22 | Your IP | Administration |
| HTTP (LLM Agent) | TCP | 8000 | 0.0.0.0/0 | Mobile app access |
| HTTP (Plan Service) | TCP | 8083 | 0.0.0.0/0 | Mobile app access |
| All Outbound | All | All | 0.0.0.0/0 | Internet access |

### Security Group #2: Database Server (sg-db-server)

| Type | Protocol | Port | Source | Purpose |
|------|----------|------|--------|---------|
| PostgreSQL | TCP | 5432 | sg-app-server | App server only |
| SSH | TCP | 22 | sg-app-server | Admin via bastion |
| All Outbound | All | All | 0.0.0.0/0 | Updates via NAT |

### IAM Roles

**EC2-App-Server-Role:**
- CloudWatchLogsFullAccess (for logging)
- SecretsManagerReadWrite (for API keys)

**EC2-DB-Server-Role:**
- CloudWatchLogsFullAccess (for logging)
- S3 backup access (specific bucket only)

---

## 📦 Deployment Steps

### Phase 1.1: AWS Infrastructure Setup

**Estimated Time:** 30 minutes

1. **Create VPC and Subnets**
```bash
# VPC: 10.0.0.0/16
# Public Subnet: 10.0.1.0/24 (us-east-1a)
# Private Subnet: 10.0.2.0/24 (us-east-1a)
```

2. **Create Internet Gateway and NAT Gateway**
```bash
# Internet Gateway for public subnet
# NAT Gateway for private subnet (for updates)
```

3. **Create Security Groups**
```bash
# sg-app-server (rules above)
# sg-db-server (rules above)
```

4. **Create Key Pair**
```bash
# Download: oddiya-prod.pem
# chmod 400 oddiya-prod.pem
```

5. **Launch EC2 Instances**
```bash
# EC2 #1: t2.micro, Amazon Linux 2023, public subnet
# EC2 #2: t2.micro, Amazon Linux 2023, private subnet
```

6. **Allocate and Associate Elastic IP**
```bash
# Allocate Elastic IP for EC2 #1
# Associate with EC2 #1
```

### Phase 1.2: Database Server Setup (EC2 #2)

**Estimated Time:** 20 minutes

**Connect via SSH tunnel through EC2 #1:**
```bash
# From local machine
ssh -i oddiya-prod.pem -L 2222:10.0.2.x:22 ec2-user@<EC2-1-PUBLIC-IP>

# Then in another terminal
ssh -i oddiya-prod.pem -p 2222 ec2-user@localhost
```

**Install PostgreSQL:**
```bash
# Update system
sudo dnf update -y

# Install PostgreSQL 17
sudo dnf install -y postgresql17 postgresql17-server

# Initialize database
sudo postgresql-setup --initdb

# Start and enable service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Configure PostgreSQL:**
```bash
# Edit postgresql.conf
sudo nano /var/lib/pgsql/17/data/postgresql.conf

# Changes:
listen_addresses = '10.0.2.x'  # Private IP of EC2 #2
max_connections = 20
shared_buffers = 256MB

# Edit pg_hba.conf
sudo nano /var/lib/pgsql/17/data/pg_hba.conf

# Add:
host    oddiya    admin    10.0.1.x/32    scram-sha-256  # EC2 #1 IP

# Restart PostgreSQL
sudo systemctl restart postgresql
```

**Create Database and User:**
```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE oddiya;
CREATE USER admin WITH PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE oddiya TO admin;

# Create schemas
\c oddiya
CREATE SCHEMA auth_service;
CREATE SCHEMA user_service;
CREATE SCHEMA plan_service;
CREATE SCHEMA video_service;

GRANT ALL ON SCHEMA auth_service TO admin;
GRANT ALL ON SCHEMA user_service TO admin;
GRANT ALL ON SCHEMA plan_service TO admin;
GRANT ALL ON SCHEMA video_service TO admin;

\q
```

**Run Flyway Migrations:**
```bash
# Copy migration files from local
scp -i oddiya-prod.pem -r services/plan-service/src/main/resources/db/migration ec2-user@<EC2-2-IP>:/tmp/

# Run migrations (use Flyway or manual SQL)
sudo -u postgres psql -d oddiya -f /tmp/migration/V1__create_travel_plans.sql
sudo -u postgres psql -d oddiya -f /tmp/migration/V2__create_plan_details.sql
```

### Phase 1.3: Application Server Setup (EC2 #1)

**Estimated Time:** 45 minutes

**Connect:**
```bash
ssh -i oddiya-prod.pem ec2-user@<EC2-1-PUBLIC-IP>
```

**Install Java 21:**
```bash
sudo dnf install -y java-21-amazon-corretto java-21-amazon-corretto-devel
java -version  # Should show 21.x.x
```

**Install Python 3.11:**
```bash
sudo dnf install -y python3.11 python3.11-pip python3.11-devel
python3.11 --version
```

**Create Application Directories:**
```bash
sudo mkdir -p /opt/oddiya/plan-service
sudo mkdir -p /opt/oddiya/llm-agent
sudo chown -R ec2-user:ec2-user /opt/oddiya
```

**Deploy Plan Service (Java):**
```bash
# Build locally
cd services/plan-service
./gradlew clean build -x test

# Copy JAR to EC2
scp -i oddiya-prod.pem build/libs/plan-service-*.jar ec2-user@<EC2-1-IP>:/opt/oddiya/plan-service/app.jar

# Create application.yml
cat > /opt/oddiya/plan-service/application.yml <<EOF
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://10.0.2.x:5432/oddiya?currentSchema=plan_service
    username: admin
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

llm-agent:
  base-url: http://localhost:8000
EOF

# Create systemd service
sudo nano /etc/systemd/system/plan-service.service
```

**plan-service.service:**
```ini
[Unit]
Description=Oddiya Plan Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/oddiya/plan-service
Environment="DB_PASSWORD=your-secure-password"
ExecStart=/usr/bin/java -Xmx512m -jar /opt/oddiya/plan-service/app.jar --spring.config.location=/opt/oddiya/plan-service/application.yml
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

**Deploy LLM Agent (Python):**
```bash
# Copy application files
cd services/llm-agent
tar czf llm-agent.tar.gz src/ requirements.txt main.py

scp -i oddiya-prod.pem llm-agent.tar.gz ec2-user@<EC2-1-IP>:/opt/oddiya/llm-agent/

# On EC2, extract and install dependencies
cd /opt/oddiya/llm-agent
tar xzf llm-agent.tar.gz
python3.11 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Create .env file
cat > .env <<EOF
GOOGLE_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.0-flash-exp
REDIS_HOST=localhost  # Not used yet, but keep for future
REDIS_PORT=6379
EOF

# Create systemd service
sudo nano /etc/systemd/system/llm-agent.service
```

**llm-agent.service:**
```ini
[Unit]
Description=Oddiya LLM Agent
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/oddiya/llm-agent
Environment="PATH=/opt/oddiya/llm-agent/venv/bin:/usr/local/bin:/usr/bin"
ExecStart=/opt/oddiya/llm-agent/venv/bin/python main.py
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

**Start Services:**
```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable and start services
sudo systemctl enable plan-service llm-agent
sudo systemctl start plan-service llm-agent

# Check status
sudo systemctl status plan-service
sudo systemctl status llm-agent

# View logs
sudo journalctl -u plan-service -f
sudo journalctl -u llm-agent -f
```

### Phase 1.4: Verification and Testing

**Estimated Time:** 15 minutes

**1. Health Checks:**
```bash
# From EC2 #1
curl http://localhost:8000/health  # LLM Agent
curl http://localhost:8083/actuator/health  # Plan Service

# From your local machine
curl http://<EC2-1-PUBLIC-IP>:8000/health
curl http://<EC2-1-PUBLIC-IP>:8083/actuator/health
```

**2. Database Connectivity:**
```bash
# From EC2 #1, test database connection
psql -h 10.0.2.x -U admin -d oddiya -c "SELECT version();"
```

**3. End-to-End Test:**
```bash
# Create a plan
curl -X POST http://<EC2-1-PUBLIC-IP>:8083/api/v1/plans \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "destination": "Seoul",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "budget": 100000
  }'

# Get plans
curl http://<EC2-1-PUBLIC-IP>:8083/api/v1/plans -H "X-User-Id: 1"
```

**4. Update Mobile App Configuration:**
```typescript
// mobile/src/constants/config.ts
export const API_CONFIG = {
  LLM_AGENT_BASE_URL: 'http://<EC2-1-PUBLIC-IP>:8000',
  PLAN_SERVICE_BASE_URL: 'http://<EC2-1-PUBLIC-IP>:8083',
};
```

---

## 💰 Cost Estimation

| Resource | Monthly Cost | Notes |
|----------|--------------|-------|
| EC2 #1 (t2.micro) | $8.50 | After 12-month free tier |
| EC2 #2 (t2.micro) | $8.50 | After 12-month free tier |
| EBS Storage (50 GB) | $5.00 | gp3 volumes |
| Elastic IP | $3.60 | If not attached to running instance |
| NAT Gateway | $32.00 | Can be replaced with Instance NAT |
| Data Transfer | ~$5.00 | Estimate |
| **Total** | **~$62.60/mo** | **$0 during free tier** |

**Cost Optimization:**
- Use Instance NAT instead of NAT Gateway: Save $32/mo
- Stop instances when not in use (dev environment)
- Use Spot Instances: Save 70%

---

## 🔍 Monitoring and Maintenance

### CloudWatch Alarms

1. **CPU Utilization > 80%** (both instances)
2. **Memory Utilization > 90%** (both instances)
3. **Disk Space < 20%** (both instances)
4. **PostgreSQL Connection Count > 18** (EC2 #2)

### Backup Strategy

**Database Backups (EC2 #2):**
```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y-%m-%d)
BACKUP_FILE="/tmp/oddiya-backup-$DATE.sql"
S3_BUCKET="oddiya-backups"

# Create backup
pg_dump -h localhost -U admin oddiya > $BACKUP_FILE

# Compress and upload to S3
gzip $BACKUP_FILE
aws s3 cp ${BACKUP_FILE}.gz s3://${S3_BUCKET}/

# Clean up local file
rm ${BACKUP_FILE}.gz

# Schedule with cron
sudo crontab -e
# Add: 0 2 * * * /opt/scripts/backup-db.sh
```

### Log Rotation

```bash
# Configure journald log rotation
sudo nano /etc/systemd/journald.conf

# Add:
SystemMaxUse=500M
RuntimeMaxUse=100M
```

---

## 🚨 Troubleshooting

### Common Issues

**Issue 1: Service won't start**
```bash
# Check logs
sudo journalctl -u plan-service -n 50
sudo journalctl -u llm-agent -n 50

# Check if port is in use
sudo netstat -tlnp | grep 8083
sudo netstat -tlnp | grep 8000
```

**Issue 2: Cannot connect to database**
```bash
# Test from EC2 #1
telnet 10.0.2.x 5432

# Check PostgreSQL is listening
sudo netstat -tlnp | grep 5432

# Check pg_hba.conf allows connection
sudo cat /var/lib/pgsql/17/data/pg_hba.conf
```

**Issue 3: Out of memory**
```bash
# Check memory usage
free -h
top

# Reduce JVM heap
# Edit /etc/systemd/system/plan-service.service
# Change -Xmx512m to -Xmx384m
```

**Issue 4: LLM Agent timeout**
```bash
# Check Gemini API key
cat /opt/oddiya/llm-agent/.env

# Test API key manually
curl -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$GOOGLE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

---

## 📋 Pre-Deployment Checklist

- [ ] AWS account created and billing alert set
- [ ] IAM user with programmatic access created
- [ ] AWS CLI configured locally
- [ ] Gemini API key obtained (free tier)
- [ ] Database password generated (strong, 16+ chars)
- [ ] SSH key pair created and downloaded
- [ ] Local builds successful (Java + Python)
- [ ] Flyway migration files ready
- [ ] Mobile app config updated with EC2 IP
- [ ] CloudWatch monitoring enabled
- [ ] S3 bucket created for backups

---

## 🚀 Deployment Execution Order

1. ✅ **Infrastructure** (30 min) - VPC, Subnets, Security Groups, EC2 instances
2. ✅ **Database** (20 min) - PostgreSQL installation, configuration, schema creation
3. ✅ **Application** (45 min) - Java + Python deployment, systemd services
4. ✅ **Verification** (15 min) - Health checks, end-to-end testing
5. ✅ **Monitoring** (10 min) - CloudWatch alarms, backup script

**Total Estimated Time:** ~2 hours

---

## 📞 Support

- Documentation: `docs/CURRENT_IMPLEMENTATION_STATUS.md`
- AWS Support: https://console.aws.amazon.com/support
- Gemini API: https://ai.google.dev/

---

**Version:** 1.0
**Last Updated:** 2025-11-04
**Status:** Ready for Deployment
