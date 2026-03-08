

# **GEMINI Project Context & Guidelines: E-Commerce API**

## **1\. Project Overview**

* **Type:** Professional E-Commerce API destined for a multi-store platform.

* **Architecture:** Modular Monolith with a Single Database.  
* **Framework:** Jakarta EE 11\.  
* **Server:** GlassFish 8.0.0.

* **Language:** Java 25 (Modern features mandatory).

* **Reference Project:** Always refer to the proevalis-backend project to check existing architectural concepts, but adapt them from Spring to standard Jakarta EE specifications (JAX-RS, CDI, Jakarta Data). Ensure they are refactored from a multi-tenant structure to a single-tenant (single database) context.

## **2\. Infrastructure & Tech Stack**

* **Main Database (Relational):** PostgreSQL 18 for transactional business data (Users, Products, Orders).  
* **Audit Database (NoSQL):** MongoDB for tracking audits and structured logging.  
* **Cache & Session (NoSQL):** Redis for caching mechanisms and JWT token revocation.  
* **Storage:** MinIO / S3 for handling file and image storage.  
* **Documentation:** MicroProfile OpenAPI 4.0 for Swagger UI generation.

## **3\. Module Dependency Graph**

The application follows a strict modular architecture to separate concerns. Lower layers **cannot** access higher layers.

1. **core** (Base Layer)  
   * *Role:* Contains Common Exceptions, Global Handlers (JAX-RS ExceptionMapper), Contracts (Interfaces), Base Entities, and HATEOAS link configurations.  
2. **util**  
   * *Role:* Shared technical utilities (e.g., helpers, formatting).  
3. **storage**  
   * *Role:* Abstraction layer handling MinIO file storage and Redis configuration.  
4. **audit**  
   * *Role:* Centralized MongoDB logging and system audit tracking.  
5. **security**  
   * *Role:* JWT Authentication (Access \+ Refresh tokens), Hybrid PBAC Aspect implementation via **CDI Interceptors**, password hashing (Argon2), data sanitization, and Rate Limiting.

6. **iam** (Identity and Access Management)  
   * *Role:* User registration, login, profile updates, and role management (client, store\_owner, admin).

7. **payment**  
   * *Role:* Provides the central payment contract/interface that other modules interact with, alongside the mocked/simulated integrations (Stripe, PayDunya, PayPal) and payment state management.

8. **domain** (Business Logic Layer)  
   * *Role:* The core business features, divided into sub-modules to enforce the Single Responsibility Principle (SRP).  
   * **domain-catalog**: Product CRUD, multi-criteria search, categories.

   * **domain-sales**: Cart management, order processing, and shipping statuses.

   * **domain-inventory**: Automatic stock updates post-order and low-stock notifications.

   * **domain-marketing**: Coupons, promotional rules, and validities.

   * **domain-analytics**: Store owner statistics (revenues, conversion rates, top products).

9. **api** (Runtime Layer)  
   * *Role:* The JAX-RS Application configuration (@ApplicationPath). Aggregates all modules. Manages API Versioning (e.g., /api/v1)  and WAR packaging.

## **4\. Security: Hybrid PBAC (Access Control)**

The system uses a highly granular Hybrid PBAC system instead of standard RBAC.

* **Implementation:** Endpoints must be protected based on roles.

* **Annotations:** Use the custom @HasPermission(resource \= ResourceType.X, action \= PbacAction.Y) annotation on JAX-RS resource methods or CDI beans.  
* **CDI Interceptor:** Use a standard Jakarta EE @InterceptorBinding for @HasPermission combined with an @AroundInvoke interceptor class to evaluate the token claims before method execution.  
* **Slugs:** Permissions are formatted as {ressource}:{action} (e.g., product:create, order:validate).  
* **Bypass:** The platform:manage permission bypasses static checks for system administrators.  
* **Dynamic Checks:** Services must implement dynamic business rules (e.g., checking if an order is already shipped before canceling).

## **5\. Coding Standards (Strict)**

### **A. Modern Java 25 & Clean Code**

* **Principles:** Strict adherence to SOLID, DRY, KISS, and Clean Code principles is required.

* **Layers:** Clear separation between controller (JAX-RS resources), service, repository, DTO, and entities.

* **Records:** MANDATORY for all DTOs and Configuration properties.  
* **Pattern Matching:** Use switch expressions and Pattern Matching for instanceof.  
* **var:** Use for local variable type inference.

### **B. Functional Programming Style**

* **No Raw Loops:** for, while, and do-while are **FORBIDDEN**.  
  * *Use:* Stream API, Optional, or recursive helper methods.  
* **Null Safety:** Strict use of the Optional pipeline. No if (x \== null) checks.  
* **Immutability:** Fields should be final wherever possible.

### **C. Jakarta EE & RESTful Design**

* **JAX-RS:** Use standard annotations (@Path, @GET, @POST, @Produces, @Consumes).  
* **CDI Injection:** Use @Inject for dependency injection exclusively. Do not use Spring's @Autowired or @RequiredArgsConstructor.  
* **HATEOAS:** True REST compliance is required. Resources should construct and return standard HTTP responses (Response.ok(entity).build()) containing self-links and relationship links.  
* **Error Handling:** Clean error management with standardized HTTP codes mapped via JAX-RS ExceptionMapper\<T\>.

### **D. Database & PostgreSQL 18 Utilization**

The application is backed by **PostgreSQL 18**. You must explicitly leverage its modern, advanced features to push data-heavy logic to the database and optimize overall performance:

* **Advanced Data Types:** Utilize JSONB for dynamic or schema-less data (e.g., complex product attributes or flexible marketing rules) and leverage native JSON processing operators.  
* **Optimized Querying:** Prefer Common Table Expressions (CTEs), Window Functions, and advanced aggregations over in-memory Java computations.  
* **Efficient DML Operations:** Always use the RETURNING clause on INSERT, UPDATE, and DELETE statements to fetch updated states in a single database round-trip.  
* **Advanced Indexing:** Implement Partial Indexes for filtered queries, and GIN/GiST indexes for full-text searches (like the multi-criteria product catalog search).  
* **Partitioning:** Consider declarative table partitioning for high-volume historical data, such as sales records or inventory logs.

### **E. Data Access & Repositories (Jakarta Data)**

All data access layers must be implemented using the **Jakarta Data API 1.0** (jakarta.data.repository), taking advantage of the standardized, vendor-neutral repository model:

* **Core Interfaces:** Extend the standard Jakarta Data interfaces (e.g., BasicRepository or define custom interfaces annotated with @Repository).  
* **Declarative Methods:** Rely on Jakarta Data's lifecycle annotations (@Insert, @Update, @Delete, @Save) for DML operations.  
* **Querying:** Use the @Find annotation for standard data retrieval and @Query for complex, custom PostgreSQL 18 queries (including CTEs or JSONB operations).  
* **Pagination & Sorting:** Use Jakarta Data's native PageRequest, Page, and Sort records to handle the API's pagination requirements natively, integrating seamlessly with the JSON output.

## **6\. Documentation & Testing Rules**

* **Documentation Language:** All MicroProfile OpenAPI documentation (@Operation, @APIResponse), README, and architecture schemas must be provided. Swagger and JavaDoc must be in **French**.

* **Unit and feature tests:** A minimum of 30% code coverage is required for unit tests (using JUnit 5, Mockito and Cucumber).