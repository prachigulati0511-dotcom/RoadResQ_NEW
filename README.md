# RoadResQ &bull; Premium AI-Powered Emergency Roadside Assistance Platform

> **Modern Startup-Grade Autonomous Roadside Rescue Ecosystem**  
> Built with Java Spring Boot, MySQL 8.0, Leaflet OpenStreetMap, and Modern Web Standards.  
> Identity: **Midnight Navy (`#060b14`), Electric Teal (`#00f5d4`), Turquoise (`#00b4d8`), and Warm Amber (`#ffb703`)**.

---

## 1. System Overview

RoadResQ is a modern full-stack web platform engineered to eliminate delays, predatory towing costs, and confusion during automotive roadside breakdowns. It pairs an autonomous diagnostic triage engine with real-time multi-provider broadcasting and atomic First-Provider-Wins dispatching.

### Architecture Highlights

```
+----------------------------------------------------------------------------------------+
|                                    CLIENT BROWSER                                      |
|  - Modern Startup Glassmorphism UI (Midnight Navy / Electric Teal / Warm Amber)       |
|  - Live Viewfinder Camera Stream (MediaDevices API) + File Dropzone                   |
|  - Interactive Leaflet Maps & Simulated Vehicle Route Telemetry                        |
|  - Web Audio API Synthesized Dispatch Alert Chimes (Zero external media assets)        |
+-------------------------------------------+--------------------------------------------+
                                            | (REST APIs with JWT Bearer Auth)
                                            v
+----------------------------------------------------------------------------------------+
|                              SPRING BOOT APPLICATION (PORT 8080)                       |
|  - Spring WebMVC & Spring Security 6 (Stateless JWT Filter)                            |
|  - Diagnostic Inference Engine (Acoustic, Visual, Thermal & OBD-II Faults)             |
|  - Transparent Itemized Cost Calculator (Base + Travel Km + Hazard Surcharge)          |
|  - FileStorageService (UUID Sanitization & Safe Image Serving)                         |
|  - Concurrency Lock: PESSIMISTIC_WRITE (SELECT FOR UPDATE) on EmergencyRequest        |
+-------------------------------------------+--------------------------------------------+
                                            |
                                            v
+----------------------------------------------------------------------------------------+
|                                 RELATIONAL PERSISTENCE                                 |
|  - Production: MySQL 8.0 (InnoDB Row-Level Locking, UTF8MB4, Foreign Key Cascades)     |
|  - Local/Dev Profile: Embedded H2 In-Memory / File Database (Zero-Config Fallback)     |
+----------------------------------------------------------------------------------------+
```

---

## 2. Key Features Implemented

1. **Customer Authentication & Vehicle Management**
   - JWT-based token authentication with 24h validity.
   - User profile management and multi-vehicle garage (Make, Model, Year, License Plate, Fuel Type, Body Style).
   - Instant Demo Role Switcher: One-click toggle between Customer (Alex Rivera) and competing Certified Providers.

2. **Emergency Request Dispatch**
   - Comprehensive service categories: Flat Tyre, Battery Jumpstart, Flatbed Towing, Fuel Delivery, Engine Overheating, Mobile EV Fast-Charge, Mechanical Breakdown, Vehicle Lockout.
   - Three-level urgency indicators: Normal (Safe Shoulder), Urgent (Highway Lane), Critical Hazard (Blind Bend/Live Traffic).

3. **Camera-Based Problem Reporting**
   - Live browser camera viewfinder capture (`navigator.mediaDevices.getUserMedia`) with instant canvas freeze.
   - Drag-and-drop / file picker photo upload with thumbnail preview, zoom lightbox, and backend validation.
   - Photos securely stored and displayed directly to the assigned technician for pre-arrival diagnostics.

4. **Smart Breakdown Assistant (AI Diagnostic Engine)**
   - 4-step interactive triage: Acoustic Symptoms, Thermal & Visual Leaks, OBD-II Warning Lamps, and Vehicle Mobility.
   - Reports probable mechanical causes, diagnostic confidence score (e.g. 95%), critical safety protocols, and estimated base fee.
   - **"1-Click Dispatch Rescue"** transfers diagnostic findings directly into the emergency dispatch form.

5. **Intelligent Provider Matching**
   - Evaluates provider proximity via Haversine distance formula, availability status, fleet rating, and service capability matching.

6. **First-Provider-Wins Assignment (Atomic Locking)**
   - Request is broadcast simultaneously to eligible nearby providers.
   - First provider to click "Accept" acquires an atomic database row lock (`PESSIMISTIC_WRITE`).
   - If multiple providers click simultaneously, the winning transaction commits while competing providers are safely rejected with HTTP 409 Conflict and notified in real-time.
   - Verified via automated multi-threaded unit test (`ConcurrencyAssignmentTest`).

7. **Transparent Cost Estimator**
   - Itemized upfront pricing before confirmation:
     - Base Service Fee (by category)
     - Travel / Distance Fee ($3.50/km beyond 5km radius)
     - Hazard Urgency Surcharge ($15 for highway lanes, $30 for critical hazards)
     - 10% Taxes & Environmental Disposal
   - Clearly labeled Demo Pricing notice.
   - Customer approval workflow for on-site revised quotes.

8. **Trusted Emergency Contacts & Auto-Alerts**
   - Save trusted emergency contacts (name, relationship, phone, email).
   - Explicit consent checkbox to broadcast automated SMS/WhatsApp alerts with live tracking URL and GPS coordinates.

9. **Interactive Map & Simulated Route Navigation**
   - Interactive Leaflet and OpenStreetMap rendering with custom neon beacon markers.
   - Live route polyline connecting assigned technician with customer location.
   - Real-time simulated vehicle progression animating the provider truck towards the customer, dynamically updating remaining distance and ETA minutes.
   - Prominently labeled: *"Mode: Real-Time Route Simulation (Leaflet OpenStreetMap)"*.

10. **Provider Portal & Radar Dashboard**
    - Instant Online/Offline availability toggle.
    - Incoming emergency request broadcast pool with audio chime notifications.
    - Active mission terminal: Customer details, address, problem photo viewer, status update buttons (Start Driving, Mark Arrived, Service Complete).
    - Revised quote submission modal with mandatory diagnostic explanation.

11. **Live Request Tracking & Timeline**
    - 5-stage progress timeline: `Pending Dispatch` ➔ `Assigned to Provider` ➔ `On the Way` ➔ `Arrived on Scene` ➔ `Completed`.
    - Auto-polling every 3 seconds ensures customer and provider dashboards remain 100% in sync without manual reloads.
    - Cancellation handling with recorded user reason.

12. **Ratings, Reviews & Service History**
    - Interactive 5-star rating modal triggered automatically upon service completion.
    - Compliment badges (*"Prompt Arrival"*, *"Professional Mechanic"*, *"Clean Towing"*, *"Transparent Pricing"*).
    - Historical receipts with itemized breakdown and technician details.

---

## 3. Technology Stack

- **Backend Framework**: Java 21+ / Spring Boot 3.3.4
- **Security**: Spring Security 6 + JJWT 0.12.6 (Stateless JWT Authentication, BCrypt Password Encoding)
- **Database & ORM**: Spring Data JPA + Hibernate ORM (MySQL 8.0 Driver + H2 Database)
- **Concurrency Control**: JPA Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) with Read Committed Transaction Isolation
- **Frontend**: Vanilla ES6 Modules + HTML5 + CSS3 Custom Design System (Zero Node.js build dependencies)
- **Maps & Geolocation**: Leaflet 1.9.4 + OpenStreetMap Tile Layer + Nominatim Reverse Geocoding
- **Icons & Audio**: Lucide Icons + Web Audio API Synthesizer (Zero external audio file dependencies)

---

## 4. Local Setup & Quickstart

### Prerequisites
- **Java JDK 21 or higher** (`java -version`)
- **Apache Maven 3.8+** (`mvn -v`)
- *(Optional for Production)* **MySQL 8.0** or **Docker**

### Option A: Run with Embedded Database (Instant Zero-Config)
The project includes a lightweight, pre-configured H2 database profile with full seed data (customers, vehicles, certified rescue providers):

```bash
cd RoadResQ_NEW
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Open your browser at:
`http://localhost:8080`

### Option B: Run with MySQL 8.0
1. Create a MySQL database (or let Spring Boot auto-create it):
   ```sql
   CREATE DATABASE IF NOT EXISTS roadresq_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Set your MySQL credentials using environment variables:
   ```powershell
   # Windows PowerShell
   $env:DB_URL="jdbc:mysql://localhost:3306/roadresq_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   mvn spring-boot:run
   ```

### Option C: Run with Docker Compose
To launch both MySQL 8.0 and RoadResQ in containers:
```bash
docker-compose up --build
```

---

## 5. Seed Accounts for Testing

The system automatically initializes verified test accounts with BCrypt-hashed credentials:

| Role | Name | Email | Password | Details |
|---|---|---|---|---|
| **Customer** | Alex Rivera | `customer@roadresq.com` | `password123` | Tesla Model 3 & RAV4 Hybrid |
| **Provider 1** | Capt. Dave Sullivan | `provider1@roadresq.com` | `password123` | Apex Heavy Towing (Flatbed Carrier) |
| **Provider 2** | Marcus Vance | `provider2@roadresq.com` | `password123` | QuickPulse Mechanics (Battery & Fuel) |
| **Provider 3** | Seraphina Chen | `provider3@roadresq.com` | `password123` | VoltCharge EV ResQ (DC Mobile Fast-Charge) |
| **Provider 4** | Leo Rodriguez | `provider4@roadresq.com` | `password123` | Metro Road Rescue (Wheel-Lift Towing) |

> **Pro Tip**: Use the **"Quick Demo Switcher"** chip in the top navigation bar to switch identities in 1 click!

---

## 6. Testing

Execute all automated unit and concurrency integration tests:

```bash
mvn test
```

### Verified Test Suite:
1. `ConcurrencyAssignmentTest`: Spawns 5 simultaneous threads to accept the exact same pending emergency call at the exact same millisecond. Validates that exactly ONE provider wins and all 4 competing providers receive conflict exceptions.
2. `DiagnosticServiceTest`: Validates that overheating, flat tyre, and dead battery symptom combinations correctly evaluate to their respective services and safety protocols.
3. `CostEstimatorTest`: Validates base fees, distance rates, hazard surcharges, taxes, and pricing transparency.

---

## 7. Production Deployment Guide

### Environment Variables
Configure the following environment variables in your production host (e.g. AWS ECS, Render, Railway, Heroku):

| Variable | Description | Example / Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `mysql` |
| `PORT` | Web server port | `8080` |
| `DB_URL` | JDBC Connection URL | `jdbc:mysql://db.prod:3306/roadresq_db` |
| `DB_USERNAME` | Database username | `roadresq_user` |
| `DB_PASSWORD` | Database password | *(Never hardcode)* |
| `JWT_SECRET` | HMAC-SHA256 Secret (Min 256 bits) | *(Secure generated token)* |

### Production Build (Standalone JAR)
```bash
mvn clean package -DskipTests
java -jar target/roadresq-backend-1.0.0.jar
```
