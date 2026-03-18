# AGENTS.md — navikt/tiltaksgjennomforing-api

## Repository Overview

REST API backend for NAV's tiltaksgjennomføring system — manages employment-measure agreements ("avtaler") between NAV, employers, and participants. Supports multiple tiltakstyper (e.g. arbeidstrening, lønnstilskudd, mentor, inkluderingstilskudd, sommerjobb, VTAO). Integrates with Altinn (employer auth), POAO-tilgang (NAV employee access control), Unleash (feature toggles), Kafka (event streaming), PDL (person data), and Dokgen (PDF generation).

## Tech Stack

- **Java 21** with Spring Boot 3.x (not Kotlin)
- **PostgreSQL** (prod) / H2 in-memory (tests)
- **Flyway** for database migrations (`src/main/resources/db/migration/`)
- **Apache Kafka** with Avro/Schema Registry for event streaming
- **Lombok** — used extensively (`@Data`, `@Slf4j`, `@RequiredArgsConstructor`, `@Builder`)
- **Unleash** for feature toggles
- **WireMock** for external-service mocking in tests
- **ShedLock** for distributed scheduled jobs
- Dockerfile / NAIS deployment

## Build & Test Commands

```bash
mvn install      # Build and package (produces target/*.jar)
mvn compile      # Compile only
mvn test         # Run tests (uses H2 + EmbeddedKafka)
```

## Architecture

### Key packages under `no.nav.tag.tiltaksgjennomforing`

| Package | Responsibility |
|---|---|
| `avtale/` | Core domain: `Avtale`, `AvtaleInnhold`, `AvtaleController`, `AvtaleRepository` |
| `avtale/events/` | Spring application events published by `Avtale` (e.g. `AvtaleInngått`, `GodkjentAvVeileder`) |
| `avtale/jobber/` | Scheduled jobs (`AvtalestatusEndretJobb`, `GjeldendeTilskuddsperiodeJobb`) |
| `autorisasjon/` | Auth/authz: `TokenUtils`, `InnloggingService`, POAO-tilgang, Altinn |
| `infrastruktur/kafka/` | Kafka config; topic names in `Topics.java` |
| `infrastruktur/auditing/` | `@AuditLogging` annotation + AOP aspect |
| `tilskuddsperiode/` | Subsidy period handling and Kafka producers/consumers |
| `datadeling/` | Outbound Kafka event publishing (`AvtaleMeldingKafkaProdusent`) |
| `varsel/` | SMS and notification handling |
| `featuretoggles/` | Unleash; all toggle names in `FeatureToggle.java` enum |

### Domain model

`Avtale` is the central aggregate. Per-tiltakstype logic lives in `AvtaleInnholdStrategy` implementations selected by `AvtaleInnholdStrategyFactory` (switch on `Tiltakstype` enum). Available tiltakstyper: `ARBEIDSTRENING`, `MIDLERTIDIG_LONNSTILSKUDD`, `VARIG_LONNSTILSKUDD`, `MENTOR`, `INKLUDERINGSTILSKUDD`, `SOMMERJOBB`, `VTAO`, `FIREARIG_LONNSTILSKUDD`.

### Authentication

JWT issuers (configured in `no.nav.security.jwt`):
- `tokenx` — external users (deltaker, arbeidsgiver, mentor) calling end-user endpoints
- `aad` — NAV internal users (veileder, beslutter) calling regular internal endpoints
- `azure-access-token` — NAV admins calling admin/operations endpoints protected with `@ProtectedWithClaims`
- `system` — system/service accounts (e.g. scheduled jobs, integrations)

Controllers are secured with `@Protected` (from `token-validation-spring`). Admin/operations controllers that require specific Azure AD groups use `@ProtectedWithClaims` with tokens from the `azure-access-token` issuer; group membership is configured via `AdGruppeProperties`. Access control for NAV employees uses POAO-tilgang (`TilgangskontrollService`); employer access uses Altinn (`AltinnTilgangsstyringService`).

### Environment profiles

Defined in `Miljø.java`: `local`, `test`, `dev-fss`, `prod-fss`, `dockercompose`, `wiremock`, `testdata`, `masse-testdata`. The `MILJO` env var selects the active profile at startup.

### Kafka topics

All topic names are constants in `Topics.java` (e.g. `TILSKUDDSPERIODE_GODKJENT`, `AVTALE_HENDELSE`, `TILTAK_SMS`).

### Database migrations

Flyway scripts in `src/main/resources/db/migration/common/` follow the naming convention `V{number}__{description}.sql`. Vendor-specific overrides live in `db/migration/postgresql/`.

## Code Standards

- **Java** (not Kotlin) — follow standard Java conventions
- Use Lombok annotations consistently; avoid manual getters/setters
- Use sealed interfaces for access-control types (see `Tilgang.java`)
- Repository queries use JPQL (`@Query` annotations) or Spring Data method names — never raw string concatenation
- Use `FeilkodeException(Feilkode.*)` for domain validation errors; add new codes to `Feilkode.java` enum
- Annotate controller methods that read personal data with `@AuditLogging`
- Feature toggles: add new toggles to the `FeatureToggle` enum; use `FeatureToggleService.isEnabled()`
- Scheduled jobs must use `@SchedulerLock` (ShedLock) to prevent concurrent execution

## Testing

- Integration tests: `@SpringBootTest`, `@ActiveProfiles({Miljø.TEST, Miljø.WIREMOCK})`, `@EmbeddedKafka`, `@DirtiesContext`
- H2 in-memory DB is configured in `src/test/resources/config/application-test.yaml`
- External services are mocked via WireMock; see `IntegrasjonerMockServer.java`
- Test fixtures live in `TestData.java` (domain objects) and `TestDataGenerator.java`
- Unit tests for domain logic do not need Spring context (e.g. `AvtaleTest`, `VeilederTest`)

## Boundaries

### ✅ Always

- Follow existing code patterns
- Run tests before committing
- Use parameterized queries for database access

### ⚠️ Ask First

- Changing authentication mechanisms
- Modifying production configurations
- Adding new dependencies (add to `pom.xml`; versions go in `<properties>`)

### 🚫 Never

- Commit secrets or credentials to git
- Skip input validation
- Bypass security controls
