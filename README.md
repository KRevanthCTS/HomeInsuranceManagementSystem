# Home Insurance Management System — Microservices

Spring Boot 4.0.7 microservices backend with a Eureka discovery server and a Spring Cloud
Gateway. Each service owns its own MySQL database (database-per-service) and services talk to
each other by name through Eureka (using OpenFeign).

## Services

| Service | Port | Database | Responsibility |
|---|---|---|---|
| `eureka-server` | 8761 | — | Service registry / discovery |
| `api-gateway` | 8080 | — | Single entry point + central JWT validation |
| `auth-service` | 8081 | `home_insurance` | Register / login users, issue JWTs |
| `customer-service` | 8082 | `home_insurance_customer` | Customer profiles |
| `policy-service` | 8083 | `home_insurance_policy` | Properties, policies, premium calc, payments, policy PDF |
| `claim-service` | 8084 | `home_insurance_claim` | Claim submission + admin review |
| `notification-service` | 8085 | `home_insurance_notification` | Claim-status notifications |

### How the pieces talk

```
                         ┌────────────────┐
  client  ──JWT──▶       │  api-gateway   │  :8080   (validates JWT once,
                         └───────┬────────┘           forwards X-User-Email / X-User-Role)
                                 │  lb:// (via Eureka)
   ┌──────────┬──────────┬───────┴────────┬───────────────┐
   ▼          ▼          ▼                ▼               ▼
 auth     customer     policy ──Feign──▶ customer      claim ──Feign──▶ policy
 :8081     :8082       :8083   (PDF)                    :8084          (validate)
                                                          └──Feign──▶ notification
                                                                        :8085
  every service ──registers──▶ eureka-server :8761
```

- **Auth is central at the gateway.** The gateway checks the `Bearer` token on every request
  except the public auth endpoints, then adds `X-User-Email` and `X-User-Role` headers that the
  downstream services trust for auditing. Downstream services do not re-validate the token.
- **Inter-service calls** (OpenFeign, resolved through Eureka):
  - `policy-service → customer-service` — fetch customer details for the policy PDF.
  - `claim-service → policy-service` — validate the policy number when a claim is filed.
  - `claim-service → notification-service` — notify the customer on submit / status change.

## What changed in the pre-written `auth-service`

The original `auth-service` had auth, policy and claim code mixed together. As agreed, the
policy/claim code was moved out and the service is now auth-only:

- **Deleted** (Policy was a stub; Claim moved to the new `claim-service`):
  - `controller/PolicyController.java`
  - `controller/ClaimController.java`
  - `service/ClaimService.java`
  - `repository/ClaimRepository.java`
  - `entity/Claim.java`
  - `dto/ClaimRequest.java`
- **`pom.xml`** — bumped Spring Boot `4.0.6 → 4.0.7`; added the Spring Cloud BOM
  (`2025.1.2`) and `spring-cloud-starter-netflix-eureka-client`.
- **`application.properties`** — `server.port` changed `8080 → 8081` (the gateway now owns 8080)
  and added the Eureka registration URL.

Everything else in `auth-service` (the JWT stack, `SecurityConfig`, `User`, `AuthService`,
`AuthController`) was left as-is.

### Note on schema additions
`ddl-auto=update` lets Hibernate create each service's tables. A few columns were added beyond
`DB_Schema_Updated.sql` to satisfy the brief:
- `properties.high_risk_area` (boolean) — drives the "+0.3 high-risk area" premium rule.
- `customers.full_name` — denormalised copy of the user's name so the PDF doesn't need an extra hop.
- `claims.customer_email`, `claims.policy_number`, `claims.description` — needed for notifications/traceability.

## Prerequisites

- Java 17+ (tested with JDK 24)
- Maven 3.9+
- MySQL running on `localhost:3306`

## Environment variables

Set these before starting the services (they are read from the environment, never hard-coded):

```bash
# Windows PowerShell
$env:MYSQL_ROOT_PASSWORD = "your-mysql-password"
$env:JWT_SECRET          = "a-long-random-secret-at-least-32-bytes-please"
$env:JWT_EXPIRATION      = "3600000"   # token lifetime in ms (1 hour)
```

`JWT_SECRET` **must be identical** for `auth-service` (signs) and `api-gateway` (verifies).

## Run order

Start Eureka first, then the gateway, then the services (order among services doesn't matter):

```bash
# 1. discovery server
cd eureka-server        && mvn spring-boot:run
# 2. gateway
cd api-gateway          && mvn spring-boot:run
# 3. business services (each in its own terminal)
cd auth-service         && mvn spring-boot:run
cd customer-service     && mvn spring-boot:run
cd policy-service       && mvn spring-boot:run
cd claim-service        && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

Eureka dashboard: http://localhost:8761 — you should see all six services registered.

All API calls go through the gateway on **http://localhost:8080**.

## Example flow (all via the gateway)

```bash
# 1. Register + login (public)
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" \
  -d '{"fullName":"Asha R","email":"asha@example.com","password":"pass123","role":"CUSTOMER"}'

TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"email":"asha@example.com","password":"pass123"}' | jq -r .token)

# 2. Create a customer profile (userId = the id from auth)
curl -X POST http://localhost:8080/customers -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"fullName":"Asha R","age":34,"phoneNumber":"9990001111","address":"MG Road, Pune"}'

# 3. Register a property, then buy a policy (premium is auto-calculated)
curl -X POST http://localhost:8080/properties -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"propertyType":"HOUSE","builtUpArea":1800,"constructionYear":2000,"propertyValue":5000000,"highRiskArea":false,"city":"Pune"}'

curl -X POST http://localhost:8080/policies -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"propertyId":1,"policyType":"FIRE","coverageAmount":4000000}'

# 4. Download the policy PDF
curl -L http://localhost:8080/policies/1/document -H "Authorization: Bearer $TOKEN" -o policy.pdf

# 5. File a claim (notification-service gets pinged automatically)
curl -X POST http://localhost:8080/claims -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"policyNumber":"HIP-2026-000123","incidentType":"FIRE","incidentDate":"2026-07-01","estimatedLoss":150000}'

# 6. Admin reviews it (needs an ADMIN token)
curl -X PUT http://localhost:8080/claims/1/status -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"APPROVED","remarks":"Verified"}'
```

## Premium formula (implemented in `PremiumCalculator`)

```
Premium = 0.5% of Property Value × Risk Factor
Risk Factor = 1.0 apartment | 1.2 house   (+0.2 if > 20 years old)  (+0.3 if high-risk area)
```
e.g. house worth 50,00,000, 26 years old, normal area → 1.2 + 0.2 = 1.4 → 0.005 × 5,000,000 × 1.4 = **35,000/yr**.
