# 📌 Patient Service


# Patient Service 🧑‍⚕️

The **Patient Service** is a Spring Boot microservice that manages patient-related functionality in the healthcare application.  
It provides APIs for patient profile management, doctor discovery, appointment booking, and accessing prescriptions.

---

## 🚀 Features
- Patient registration & profile management
- View & update patient profile
- Doctor discovery (search doctors by city, specialization, availability)
- View doctor profile, reviews & ratings
- Book appointments with doctors
- View prescriptions issued by doctors
- Add reviews for doctors
- (Stub) Payment intent creation for appointment booking → will later integrate with **payment-service**

---

## 🏗️ Project Structure

```
patient-service
├── src/main/java/com/healthcare/patient
│ ├── config # Swagger/OpenAPI and other configs
│ ├── controller # REST controllers
│ ├── dto # Request/response DTOs
│ ├── entity # JPA entities
│ ├── exceptions # Custom exceptions & handlers
│ ├── repo # Spring Data JPA repositories
│ ├── service # Business logic
│ └── PatientServiceApplication.java
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
http://localhost:8082/swagger-ui/index.html

---

## 🔒 Security & Roles

Patient authentication will be managed by API Gateway + JWT.

Controller methods requiring patient roles are marked with // TODO: role check.

---

## 📚 API Endpoints (Examples)

```
POST /api/patients → Register patient

PUT /api/patients/{patientId}/profile → Update patient profile

GET /api/patients/doctors/search?city=Mumbai&specialization=Dermatologist → Discover doctors

POST /api/patients/{patientId}/appointments → Book an appointment

GET /api/patients/{patientId}/prescriptions → View prescriptions

POST /api/patients/{patientId}/reviews → Add review for doctor
```
---

## 📝 Notes

Doctor discovery APIs internally call doctor-service.

Appointment booking will later integrate with payment-service (stub present for now).

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
APPOINTMENT_BASE_URL=http://localhost:8084
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/patient_service
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=Abhi@123
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
APPOINTMENT_BASE_URL=http://localhost:8084
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/patient_service
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=Abhi@123
```

Then your .env file values will override them.