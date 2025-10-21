# Doctor Service 🩺

The **Doctor Service** is a Spring Boot microservice that manages doctor-related functionality in the healthcare application.  
It provides APIs for doctor profile management, availability scheduling, prescription management, and patient reviews.

---

## 🚀 Features
- Doctor registration & profile management
- Manage clinic addresses, specialization, consultation fee, contact details
- Availability slot management (create, update, block/unblock)
- Create & manage prescriptions for patients
- Manage doctor reviews and ratings (patients can post reviews)
- Search doctors by specialization, city, availability
- API documentation with **Swagger / OpenAPI**

---

## 🏗️ Project Structure
```
doctor-service
├── src/main/java/com/healthcare/doctor
│ ├── config # Swagger/OpenAPI and other configs
│ ├── controller # REST controllers
│ ├── dto # Request/response DTOs
│ ├── entity # JPA entities
│ ├── exceptions # Custom exceptions & handlers
│ ├── repo # Spring Data JPA repositories
│ ├── service # Business logic
│ └── DoctorServiceApplication.java
└── src/main/resources
├── application.yml
└── data.sql (optional for seed data)
```
---

## ⚙️ Tech Stack
- Java 17+
- Spring Boot 3+
- Spring Data JPA (Hibernate)
- PostgreSQL / MySQL
- Maven
- Lombok
- Swagger / OpenAPI 3

---

## 🌐 Swagger UI available at:
http://localhost:8083/swagger-ui/index.html

---

## 🔒 Security & Roles

APIs requiring doctor authentication will later be secured via API Gateway + JWT.

For now, role-based access checks are marked in the controller with // TODO: role check.

---

## 📚 API Endpoints (Examples)
```
POST /api/doctors → Register a doctor

PUT /api/doctors/{doctorId}/profile → Update doctor profile

POST /api/doctors/{doctorId}/availability/slots → Add availability slots

PUT /api/doctors/{doctorId}/availability/slots/block → Block/unblock slots

POST /api/doctors/{doctorId}/prescriptions → Create prescription

GET /api/doctors/search?city=Delhi&specialization=Cardiologist → Search doctors
```
---

## 📝 Notes

This service will integrate with the appointment-service for handling doctor appointments, and with the review-service (or internal review module) for reviews

In our current design, reviews are stored within doctor-service (as part of the doctor domain). We didn’t create a separate review-service yet.
Later, if reviews become complex (moderation, analytics, social features, huge scale), we can move them into a separate review-service, but right now they belong to doctor-service.

- Doctor-service → integrates with appointment-service (for appointments)

- Reviews → currently part of doctor-service (not a separate service) ✅

Payment-related actions are deferred to payment-service.

---

## ✅ If you don’t want .env to affect your app:

Do NOT add spring-dotenv dependency yet.

Without it, Spring Boot will completely ignore .env.

Only application.yml will be considered.

Keep your .env file in project root (just for reference or future use).
```
# Environment variables & config to set

KAFKA_BOOTSTRAP=localhost:9092
APPOINTMENT_BASE_URL=http://localhost:8084
REVIEW_BASE_URL=http://localhost:8088
SPRING_PROFILES_ACTIVE=dev
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
# Environment variables & config to set

KAFKA_BOOTSTRAP=localhost:9092
APPOINTMENT_BASE_URL=http://localhost:8084
REVIEW_BASE_URL=http://localhost:8088
SPRING_PROFILES_ACTIVE=dev
```

Then your .env file values will override them.