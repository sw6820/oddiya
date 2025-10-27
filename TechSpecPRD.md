다음은 "claude code"가 초기 개발에 참고할 수 있도록 `v1.3` 아키텍처를 기반으로 작성된 영문 기술 기획서입니다.

-----

## Oddiya Project: Developer Kickoff (v1.3 - Hybrid Self-Hosted)

**Document Version:** 1.3
**Date:** 2025-10-27
**Target:** Initial Development (8-Week MVP)
**Author:** (User Name) / Gemini Architect

### 1\. Project Overview

  * **Project:** Oddiya
  * **Mission:** An AI-powered mobile travel planner (using real-world data) and an automated short-form video generator based on user photos.
  * **Development Priorities:**
    1.  **P1: Core Flow:** OAuth Login -\> AI-Powered Travel Planning.
    2.  **P2: K8s Operations:** Deploying and managing stateless microservices on EKS.
    3.  **P3: Video Generation:** Asynchronous video processing pipeline.
  * **Timeline:** 8 Weeks (Stage 1 MVP)

### 2\. Core Technology Stack

  * **Backend (Java):** Spring Boot 3.2, Java 21, Spring Cloud Gateway, Spring Security
  * **Backend (Python):** FastAPI (LLM Agent), Python 3.11 (Video Worker)
  * **Orchestration:** **AWS EKS** (Kubernetes 1.28)
  * **K8s Compute Node:** **1x EC2 `t3.medium` (Spot Instance)**
  * **Database (Self-Hosted):** **PostgreSQL 17.0** (on a dedicated **`t2.micro` EC2**)
  * **Cache (Self-Hosted):** **Redis 7.4** (on a dedicated **`t2.micro` EC2**)
  * **Storage:** **AWS S3**
  * **Messaging:** **AWS SQS** (Standard Queue + DLQ)
  * **AI:** **AWS Bedrock** (Claude Sonnet)
  * **External APIs:** Kakao Local, OpenWeatherMap, ExchangeRate-API

### 3\. Infrastructure Architecture (v1.3)

This hybrid model solves the memory exhaustion risk of v1.2. The `t3.medium` EKS node (4GB RAM) is now **only responsible for stateless applications**, which is a much more stable and realistic configuration.

#### 🚨 CRITICAL WARNING: `t2.micro` Database Bottleneck

  * A `t2.micro` instance has only **1GB of RAM**.
  * Running PostgreSQL 17 on 1GB RAM will be **extremely slow**. It will be the **primary performance bottleneck** of the entire system.
  * It also uses burstable T2 CPU credits. If credits run out, performance will be severely throttled.
  * **Trade-off:** We are trading **performance** for **cluster stability** and **low cost**. For this 8-week learning project, this is an accepted trade-off.

#### Architecture Diagram

```mermaid
graph TD
    subgraph "Mobile Client"
        Mobile[📱<br>React Native App<br>(Google/Apple OAuth)]
    end

    subgraph "AWS Cloud (External Services)"
        ALB(🌐<br>AWS ALB)
        S3[📦<br>AWS S3<br>(Photos, Videos)]
        SQS[📤<br>AWS SQS<br>(Video Jobs)]
        SNS[🔔<br>AWS SNS<br>(Push Notify)]
        Bedrock[🧠<br>AWS Bedrock<br>(Claude Sonnet)]
    end
    
    subgraph "AWS VPC (Private Subnet)"
        subgraph "AWS EKS Cluster (1x t3.medium Spot Node)"
            Ingress(🚦<br>Nginx Ingress)
            subgraph "Stateless Services (Deployments)"
                Gateway(API Gateway)
                Auth(Auth Service)
                User(User Service)
                Plan(Plan Service)
                Video(Video Service)
                LLM(LLM Agent)
                Worker(Video Worker)
            end
        end

        subgraph "Self-Hosted Stateful (2x t2.micro EC2)"
            PostgreSQL[🐘<br>EC2 (t2.micro)<br>PostgreSQL 17.0]
            Redis[⚡️<br>EC2 (t2.micro)<br>Redis 7.4]
        end
    end

    %% --- Public Flows ---
    Mobile --> ALB
    Mobile -- Uploads --> S3
    SNS --> Mobile

    %% --- K8s Ingress Flow ---
    ALB --> Ingress
    Ingress --> Gateway

    %% --- K8s Internal Flows ---
    Gateway --> Auth
    Gateway --> User
    Gateway --> Plan
    Gateway --> Video
    
    Auth --> User
    Plan --> LLM
    Video --> SQS
    Worker -- Polls --> SQS
    Worker --> SNS
    LLM --> Bedrock

    %% --- Stateful Connections (Private VPC) ---
    Auth -- port 6379 --> Redis
    LLM -- port 6379 --> Redis
    
    Auth -- port 5432 --> PostgreSQL
    User -- port 5432 --> PostgreSQL
    Plan -- port 5432 --> PostgreSQL
    Video -- port 5432 --> PostgreSQL
    Worker -- port 5432 --> PostgreSQL

    %% --- S3 Flows ---
    Worker -- Downloads --> S3
    Worker -- Uploads --> S3
```

### 4\. Microservice Architecture

The system is decomposed into 7 stateless services running in EKS.

1.  **API Gateway (Spring Cloud Gateway):**
      * Single entry point for all mobile traffic.
      * **Responsibility:** Routes requests, performs rate limiting, and **validates RS256 JWTs** using the public key from `Auth Service`'s `/jwks.json` endpoint.
2.  **Auth Service (Spring Boot):**
      * **Responsibility:** Handles all authentication and token generation.
      * **Flow:** Manages OAuth 2.0 (Google, Apple), exchanges codes, and calls `User Service` to create/get user.
      * **Output:** Generates `RS256` Access Tokens (1hr) and Refresh Tokens (14 days, stored in the **`t2.micro` Redis**). Provides a `/.well-known/jwks.json` endpoint for its public key.
3.  **User Service (Spring Boot):**
      * **Responsibility:** Manages user profile data.
      * **API:** `GET /users/me`, `PATCH /users/me`.
      * **Internal API:** `POST /internal/users` (called *only* by `Auth Service` during first-time login).
      * **Database:** Connects to the **`t2.micro` Postgres**.
4.  **Plan Service (Spring Boot):**
      * **Responsibility:** Core travel plan CRUD logic.
      * **Flow:** For AI recommendations, it calls the `LLM Agent` via synchronous REST.
      * **Database:** Connects to the **`t2.micro` Postgres**.
5.  **LLM Agent (FastAPI):**
      * **Responsibility:** Orchestrates AI plan generation.
      * **Flow:**
        1.  Receives request from `Plan Service`.
        2.  Calls external APIs (Kakao Local, OpenWeatherMap).
        3.  Uses **Bedrock Function Calling** with Claude Sonnet, injecting the real-world data from the APIs.
        4.  Returns a structured JSON plan.
      * **Cache:** Caches responses in the **`t2.micro` Redis** (1hr TTL).
6.  **Video Service (Spring Boot):**
      * **Responsibility:** Manages video job lifecycle.
      * **Flow:**
        1.  Receives `POST /api/v1/videos` request (with a client-generated `Idempotency-Key` header).
        2.  Saves job to the **`t2.micro` Postgres** with `status: PENDING`.
        3.  Publishes a job message to **AWS SQS**.
        4.  Returns `202 Accepted` with the `job_id`.
7.  **Video Worker (Python):**
      * **Responsibility:** Asynchronously processes video jobs.
      * **Flow:**
        1.  Long-polls the SQS queue.
        2.  Checks DB status on the **`t2.micro` Postgres** (`if status == 'PENDING'`) to ensure idempotency.
        3.  Updates status to `PROCESSING`.
        4.  Downloads photos from S3, generates video using **FFmpeg** (Priority: 1 template first).
        5.  Uploads final MP4 to S3.
        6.  Updates status to `COMPLETED` and triggers an **AWS SNS** notification.

### 5\. Database & Schema

  * **Database:** PostgreSQL 17.0 (on a `t2.micro` EC2).
  * **Cache:** Redis 7.4 (on a `t2.micro` EC2).
  * **Design:** **Schema-per-service** model in the single PostgreSQL database.
      * `user_service.users`
      * `plan_service.travel_plans`
      * `plan_service.plan_details`
      * `video_service.video_jobs`
  * **Key Table: `video_service.video_jobs`**
      * `id` (BIGSERIAL, PK)
      * `user_id` (BIGINT)
      * `status` (VARCHAR, 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')
      * `photo_urls` (TEXT[])
      * `template` (VARCHAR)
      * `video_url` (VARCHAR)
      * `idempotency_key` (UUID, **UNIQUE NOT NULL**) (This key is provided by the mobile client)
  * **ERD:**
    ```mermaid
    erDiagram
        user_service.users {
            BIGSERIAL id PK
            VARCHAR email
            VARCHAR name
            VARCHAR provider
            VARCHAR provider_id
        }
        plan_service.travel_plans {
            BIGSERIAL id PK
            BIGINT user_id FK
            VARCHAR title
            DATE start_date
            DATE end_date
        }
        plan_service.plan_details {
            BIGSERIAL id PK
            BIGINT plan_id FK
            INT day
            VARCHAR location
            TEXT activity
        }
        video_service.video_jobs {
            BIGSERIAL id PK
            BIGINT user_id FK
            VARCHAR status
            TEXT[] photo_urls
            UUID idempotency_key UK
        }
        user_service.users ||--o{ plan_service.travel_plans : "has"
        user_service.users ||--o{ video_service.video_jobs : "owns"
        plan_service.travel_plans ||--|{ plan_service.plan_details : "contains"
    ```

### 6\. Core User Flows

#### 6.1. P1: Authentication (RS256 JWT)

1.  **Mobile App** initiates OAuth flow (`GET /oauth2/authorize/google`).
2.  **Auth Service** responds with redirect URL.
3.  **Mobile App** completes flow with Google/Apple, receives `authorization_code`.
4.  **Mobile App** sends `code` to `POST /oauth2/callback/google`.
5.  **Auth Service** exchanges `code` for Google tokens, fetches user info.
6.  **Auth Service** calls `POST /internal/users` on **User Service** (find or create user, connects to `t2.micro` Postgres).
7.  **Auth Service** generates `Access Token` (RS256, 1hr) and `Refresh Token` (UUID).
8.  **Auth Service** stores `refresh_token:uuid -> {user_id}` in the **`t2.micro` Redis**.
9.  **Mobile App** receives tokens.
10. **Mobile App** sends `Access Token` in `Authorization: Bearer` header for all future requests.
11. **API Gateway** intercepts all requests, fetches public key from `Auth Service`'s `/.well-known/jwks.json` (caches it in Redis), and validates the RS256 signature.

#### 6.2. P3: Video Generation (Asynchronous SQS + SNS)

1.  **Mobile App** generates a `client-idempotency-key` (UUID).
2.  **Mobile App** gets pre-signed URLs from `Video Service` and uploads photos directly to S3.
3.  **Mobile App** calls `POST /api/v1/videos` with photo S3 URLs, template choice, and the `Idempotency-Key` header.
4.  **Video Service** validates the request, saves the job to the **`t2.micro` Postgres** with the `idempotency_key` and `status: PENDING`. (If key already exists, it returns the existing job).
5.  **Video Service** publishes a message (containing `job_id`) to the `oddiya-video-jobs` SQS queue.
6.  **Video Service** returns `202 Accepted` to the mobile app.
7.  **Video Worker** (running in EKS) polls SQS, receives the message.
8.  **Video Worker** performs its idempotent check (checks DB `status == 'PENDING'` on `t2.micro` Postgres), then starts processing (FFmpeg).
9.  **Video Worker** uploads the final video to S3, updates the DB `status: COMPLETED` and `video_url`.
10. **Video Worker** publishes a "Job Done" message to an **AWS SNS** topic.
11. **AWS SNS** sends a **push notification** to the user's device.
12. **Mobile App** (on receiving the push) fetches the `GET /api/v1/videos/{jobId}` to get the final `video_url`. **(No client-side polling)**.

### 7\. 8-Week Development Plan (v1.3 - Stable)

This plan is **much more realistic** as it removes the high-risk K8s StatefulSet task from the critical path and aligns with the stated priorities.

  * **Week 1-2: [P1/P2] Infrastructure & Auth Foundation**

      * Set up **EKS Cluster** (with `t3.medium` Spot node).
      * Provision **2x `t2.micro` EC2s**.
      * Install **Postgres 17.0** on one (e.g., via Docker) and **Redis 7.4** on the other.
      * Configure Security Groups (EKS Nodes -\> `t2.micro` ports 5432/6379).
      * Build `Auth Service` & `User Service`.
      * Update `application.yml` to connect to the `t2.micro` private IPs.
      * **Goal:** Full E2E OAuth 2.0 (P1) and K8s (P2) base flow working.

  * **Week 3-5: [P1] Core AI Planning**

      * Build `LLM Agent` (FastAPI).
      * **Priority 1:** Integrate **Bedrock (Claude)** + **Kakao Local API**.
      * **Priority 2:** Add OpenWeatherMap & ExchangeRate-API *if time permits*.
      * Build `Plan Service` CRUD and integrate with `LLM Agent`.
      * **Goal:** AI-powered plan generation (P1) complete.

  * **Week 6-7: [P3] Asynchronous Video**

      * Build `Video Service` (API + SQS Producer).
      * Build `Video Worker` (SQS Consumer + FFmpeg).
      * **Priority 1:** Implement **one** video template ('basic').
      * **Priority 2:** Add SNS push notifications.
      * **Goal:** E2E video generation (P3) complete.

  * **Week 8: [P2] K8s Ops & Testing**

      * Write **Locust** load tests (you will immediately see the Postgres `t2.micro` bottleneck here).
      * Configure HPA (Horizontal Pod Autoscaler) for `Video Worker` and stateless services.
      * Finalize all documentation (README, API spec).
      * **Goal:** Project is documented, load-tested, and "complete".