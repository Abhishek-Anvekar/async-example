# 🗓️ Appointment Service

The **Appointment Service** is responsible for managing doctor-patient appointments in the healthcare system.  
It acts as the mediator between **doctor availability** and **patient booking requests**, ensuring smooth scheduling, cancellation, and status tracking.

---

## ⚙️ Tech Stack

- **Java 17 (LTS)**
- **Spring Boot 3.x**
- **Spring Data JPA & Hibernate**
- **PostgreSQL / MySQL**
- **OpenAPI (Swagger UI)**
- **Lombok**
- **CompletableFuture** for async workflows
- (Future) **Kafka** for notification events

---

## 📂 Project Structure
```
appointment-service/
├── src/main/java/com/healthcare/appointment
│ ├── controller/ # REST controllers
│ ├── service/ # Business logic
│ ├── repo/ # JPA repositories
│ ├── dto/ # Data Transfer Objects
│ ├── entity/ # JPA entities
│ ├── exception/ # Custom exceptions & handlers
│ ├── config/ # Swagger, async config
│ └── AppointmentServiceApplication.java
└── src/main/resources/
├── application.yml # App config
└── data.sql # Seed data (optional)
```

---

## 🚀 Features

- **Book Appointment**
    - Patients can book appointments with doctors.
    - Checks doctor availability before confirming.
- **Cancel Appointment**
    - Patients can cancel an appointment (with policies).
- **Reschedule Appointment**
    - Change appointment slot if available.
- **List Appointments**
    - For both patients and doctors.
- **Appointment Status Tracking**
    - Pending, Confirmed, Completed, Cancelled.
- **Async Processing**
    - Use of `CompletableFuture` for non-blocking operations.
- **Integration**
    - Works with `doctor-service` for availability.
    - Works with `patient-service` for patient details.
    - Will integrate with `notification-service` and `payment-service`.

---

## 📖 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/appointments` | Book a new appointment |
| `PUT`  | `/appointments/{id}/cancel` | Cancel an appointment |
| `PUT`  | `/appointments/{id}/reschedule` | Reschedule an appointment |
| `GET`  | `/appointments/patient/{patientId}` | Get all patient appointments |
| `GET`  | `/appointments/doctor/{doctorId}` | Get all doctor appointments |
| `GET`  | `/appointments/{id}` | Get appointment details |

---

## 🔒 Security

👉 Role-based access will be **handled at API Gateway**, so controllers only have endpoint logic.  
Add comments where role checks are required.

---

## 🌐 Swagger API Docs

Swagger UI available at:  
👉 http://localhost:8084/swagger-ui/index.html

---

## 📝 Notes

- This service depends on:
    - **doctor-service** for doctor profiles & availability.
    - **patient-service** for patient details.
- Notifications (SMS/Email/Push) will be handled by **notification-service**.
- Payment confirmation (consultation fees) will be handled by **payment-service**.

---

## ✅ If you don’t want .env to affect your app:

Do NOT add spring-dotenv dependency yet.

Without it, Spring Boot will completely ignore .env.

Only application.yml will be considered.

Keep your .env file in project root (just for reference or future use).
```
#  Environment variables & config to set

KAFKA_BOOTSTRAP=localhost:9092
DOCTOR_BASE_URL=http://localhost:8083
PATIENT_BASE_URL=http://localhost:8082
```

Run the app normally:

mvn spring-boot:run


It will only read from application.yml.

## ✅ Later, when you want to switch to .env:

Add the spring-dotenv dependency in pom.xml:
```
<dependency>
  <groupId>me.paulschwarz</groupId>
  <artifactId>spring-dotenv</artifactId>
  <version>3.0.0</version>
</dependency>
```

Update application.yml to use environment variables like:
```
#  Environment variables & config to set

KAFKA_BOOTSTRAP=localhost:9092
DOCTOR_BASE_URL=http://localhost:8083
PATIENT_BASE_URL=http://localhost:8082
```

Then your .env file values will override them.

---
## Flyway Implementation:

Here’s a clean, step-by-step guide to add Flyway to your appointment-service and run an ALTER on the appointments table.

Step 1: Add Flyway Dependency

In your appointment-service/pom.xml add Flyway dependency (if not already present):
```
        <!-- Flyway Core -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>10.18.0</version>
        </dependency>

        <!-- Flyway MySQL support -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
            <version>10.18.0</version>
        </dependency>
```
Step 2: Configure Flyway in application.yml

Make sure your datasource is already configured. Add Flyway properties under spring.flyway:
```
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/appointment_service
    username: root
    password: Abhi@123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

Step 3: Create Migration Folder

Inside your appointment-service project, create the folder:
```
src/main/resources/db/migration
```

Step 4: Create Migration Script

Flyway uses versioned migration files. The naming convention is:

version should be in sequence like first file ```v1```, second file ```v2``` and all files should be present at this path else get error.
```
V<version_number>__<description>.sql
```

For adding phone column, create a file:
```
src/main/resources/db/migration/V1__add_phone_to_appointment.sql
```

👉 Content of V1__add_phone_to_appointment.sql:
```
ALTER TABLE appointments
ADD COLUMN patient_phone VARCHAR(15) AFTER patient_id;
```
Step 5: Update the JPA (Appointment) entity to match the new columns
```
@Column(nullable = false)
private String patientPhone;
```
Step 6: Run Application

When you start appointment-service, Flyway will automatically detect the new migration and execute it.

---