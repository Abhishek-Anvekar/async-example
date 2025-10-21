For @Async and CompletableFuture, check --> Appointment-service main class, AsyncConfig class, AppointmentDomainService class, and ExternalServiceClient class.
For @UniqueConstraint and Optimistic Locking, check --> AvailabilitySlot entity class: @Version used for Optimistic Locking, AvailabilityService class --> check blockSlots() method for Optimistic lock exception.

---

# Spring Boot Asynchronous Processing & Optimistic Locking

---

## 🧩 1️⃣ What is `@Async` in Spring Boot?

The `@Async` annotation is used to execute a method asynchronously, i.e., in a separate thread.  
This means the caller won’t wait for the method to complete — ideal for tasks like:

- Sending emails or SMS
- Logging or audit operations
- Generating reports
- Long-running background processes

---

## 🧠 1️⃣ What is `CompletableFuture`?

`CompletableFuture` is a Java concurrency class (from `java.util.concurrent`) used to handle asynchronous results — i.e., results that will be available in the future after a background task finishes.

Think of it like a “promise” that says:

> “I’m doing this work in another thread. When I’m done, I’ll give you the result.”

---

## ⚙️ 2️⃣ Why Use `CompletableFuture` with `@Async`

When you mark a method with `@Async`, Spring executes it in a separate thread and immediately returns control to the caller.

If that async method returns `void`, the caller has no way to:

- Get the result later
- Know when it’s completed
- Handle exceptions

👉 That’s where `CompletableFuture` comes in.  
It lets you return a placeholder object that will eventually contain the actual result.

---

## 🧾 5️⃣ Summary Table

| Aspect | Explanation |
|--------|-------------|
| `@Async` | Executes the method in a new thread |
| `CompletableFuture<T>` | Represents a result that will be available later |
| `CompletableFuture.completedFuture(value)` | Immediately wraps a computed value (useful for returning an already-available result) |
| `.get()` | Waits for result (blocking) |
| `.join()` | Similar to `.get()`, but throws unchecked exceptions |
| `.allOf()` | Combines multiple futures and waits for all to complete |
| **Benefit** | Run multiple async tasks concurrently and combine results |

> `@Async` tells Spring:  
> “Run this method in a background thread, not the main thread.”

The method starts executing asynchronously.  
`CompletableFuture` gives you an object that represents the future result of that background work.

---

## 🧩 What is Optimistic Locking?

Optimistic locking assumes that most of the time, no one else is modifying the same record.  
Instead of blocking access (like pessimistic locks), it just checks **before commit** whether the record was changed by someone else.

If it detects a concurrent modification — it throws an exception (`OptimisticLockException`) and you retry or fail gracefully.

---

## 🧠 How It Works

1. When you read an `AvailabilitySlot`, JPA remembers its version (e.g., 3).
2. When you update it (e.g., mark it blocked), Hibernate generates an SQL like:

```sql
UPDATE availability_slots
SET blocked = true, version = 4
WHERE id = 'slot123' AND version = 3;
```

---

## How Optimistic Locking Works

- If no other transaction modified the row → one row is updated → ✅ success  
- If another transaction already modified it (version now = 4) →  
  `WHERE id='slot123' AND version=3` fails → 0 rows updated →  
  Hibernate throws `OptimisticLockException`

---

## ⚙️ Why `@UniqueConstraint` and `@Version` Work Fine Together

- `@UniqueConstraint` → enforced by the database engine (MySQL, PostgreSQL, etc.)  
  Ensures certain column combinations are **unique across all rows**.  
  Example: No two slots with the same doctor, date, startTime, and endTime.

- `@Version` → handled by JPA/Hibernate inside the transaction  
  Ensures that **two threads don’t update the same row at the same time**

They operate at **different layers**, so they **don’t conflict**.
