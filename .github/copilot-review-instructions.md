# Code review instructions: tiltaksgjennomforing-api

Java 21 / Spring Boot backend with JPA, Flyway, Kafka and token-support. There is no frontend code in this repository. Background, patterns and terms are in [`AGENTS.md`](../AGENTS.md).

Report real bugs, security problems and breaks with established patterns. Skip pure style preferences.

## Security

- **No secrets or personal data** in code, config, test data or logs. Flag hardcoded tokens, passwords, keys and real fødselsnummer. Tests must use synthetic data (fødselsnummer, names, addresses).
- **Logging:** ordinary `log.*` calls must not contain fødselsnummer, names, addresses, account numbers or tokens. Personal data may only be logged through `logging/TeamLogs`. Watch for whole objects (`Avtale`, DTOs, `toString()`) being logged.
- **Auth and access code gets extra scrutiny.** Changes in `autorisasjon/`, `@Protected` / `@ProtectedWithClaims`, `@EnableJwtTokenValidation(ignore = ...)`, issuer config in `application-*.yaml`, AD groups or `accessPolicy` in `nais/` must be reviewed line by line.
- **Authenticated endpoints** must have `@Protected` or `@ProtectedWithClaims` with the right issuer: `tokenx`/`aad` for users, `azure-access-token` for admin, `system` for service-to-service. Preserve intentionally public endpoints (`@Unprotected`), including health checks, feature lookup and `InternalAvtaleHendelseController.hentJsonSkjema()`. Review new authentication exemptions explicitly.
- **Ownership, not just authentication:** end-user endpoints (`tokenx`/`aad`) that read or change an agreement must resolve the caller with `InnloggingService.hentAvtalepart(...)` and check `Tilgang` (`harTilgangTilAvtale`) for that agreement. The `innlogget-part` cookie is client-controlled and must never be trusted directly. Do not demand `hentAvtalepart` on system or admin endpoints: it only accepts tokenx/aad role combinations and would reject valid callers. System endpoints must call `InnloggingService.validerSystembruker()`, and admin endpoints must restrict issuer and AD group in `@ProtectedWithClaims`.
- **Data returned to a party** must go through the existing masking (`Avtalepart.maskerFelterForAvtalepart`) or a role-specific DTO. Flag new responses that expose fields to roles that should not see them.
- Endpoints that return personal data should have `@AuditLogging`.
- **Hiding elements by role in the UI is not access control.** Do not accept "the frontend does not show this button" as a reason to skip a check in the API.
- New dev or test endpoints must be limited with `@Profile` (see `dev/DevController`) and never be active in `prod-fss`.
- **Input validation:** flag endpoints and Kafka consumers that accept input without validating it in the API. Validation happens in the domain (`FeilkodeException`, `Avtale.felterSomIkkeErFyltUt()`); "the frontend already validates this" is not a reason to skip it.
- **SMS text** must not contain fødselsnummer, names or other personal data. Existing texts in `varsel/LagSmsFraAvtaleHendelse` only contain the agreement number, measure name, deadline and a link.

## Frontend contract

The frontend ([navikt/tiltaksgjennomforing](https://github.com/navikt/tiltaksgjennomforing)) mirrors several parts of this API. Flag these changes and check that the PR describes the matching frontend change:

- Changed, renamed or removed fields in response DTOs (`avtale/transportlag/`, `autorisasjon/InnloggetBruker`).
- New or removed `Feilkode` values. The frontend shows a message per code from the `feilkode` response header.
- Removed or renamed `FeatureToggle` values. `FeatureToggleConverter` rejects unknown names, so the frontend's whole `/feature` request fails if it still asks for a removed toggle.

## Database and migrations

- Queries must use bound parameters (Spring Data methods or `@Query` with `:param`). Flag any string-built JPQL/SQL.
- `AvtaleRepository.sokEtterAvtale` and its count query must have identical filter predicates.
- Comparisons on nullable columns (for example `sluttDato`) need an explicit `IS NULL` branch when NULL rows should be included.
- Flyway: new files only, in `db/migration/common/` as `V{n}__description.sql` with the next free number. Flag edits to existing migrations, duplicate version numbers and SQL that will not run on both PostgreSQL and H2 in PostgreSQL mode (move Postgres-only SQL to `db/migration/postgresql/`).
- Flag destructive schema changes (drop, rename, `NOT NULL` without default) and large data updates without a plan.

## Domain, events and Kafka

- Business rules belong on `Avtale`, `Avtalepart` subclasses or `AvtaleInnholdStrategy`, not in controllers.
- State changes on `Avtale` should register a domain event (`registerEvent`) so listeners (varsel, datadeling, datavarehus, sporingslogg) are triggered.
- Changes to `AvtaleMelding`, Avro schemas in `src/main/resources/avro/`, or Kafka message classes are contract changes for other teams. Check that they are backwards compatible or explicitly agreed.
- Topic names belong in `Topics.java`.
- Scheduled jobs need `@SchedulerLock`. Batch jobs should page instead of loading all rows.
- Domain errors use `FeilkodeException(Feilkode.X)`. Flag generic `RuntimeException` for user-facing validation.
- Time in domain code must come from `utils/Now` (`Now.localDate()`, `Now.instant()`), not `LocalDate.now()`, so tests can control it.

## Java and Spring

- Use Lombok as the surrounding code does. Flag manual getters, setters and constructors that duplicate Lombok.
- Constructor injection (`@RequiredArgsConstructor` with `final` fields), not field injection.
- `@Transactional` on methods that change several entities or must publish events atomically.
- Feature toggles are declared in `FeatureToggle` and read with `FeatureToggleService.isEnabled(...)`.
- Tests: new behavior needs tests. Prefer unit tests without Spring context for domain logic. Reuse `TestData`. Test method names in snake_case.

## Norwegian text for users

Applies to strings that reach users: SMS texts (`varsel/LagSmsFraAvtaleHendelse`), notification and history texts (`avtale/HendelseType`), and `beskrivelse` in `Status` and `Tiltakstype`. Documentation in this repository is in English and is not covered.

- Use Norwegian bokmål and plain language (klarspråk).
- Write «Nav», never «NAV».
- Avoid unnecessary anglicisms when a good Norwegian word exists.

## Local Docker

- The team runs Docker through Colima (`colima start`), not Docker Desktop. Flag documentation or scripts that assume Docker Desktop.
- `docker-compose.yml` only starts Postgres for `DockerComposeTiltaksgjennomforingApplication`. Flag new services added without a clear local need.

## GitHub Actions

- `actions/*` (immutable releases), `nais/*` (Nav) and `github/*` (GitHub) are trusted and may use version tags.
- All other third-party actions must be pinned to a full commit SHA with a version comment, for example `uses: owner/action@<40-char-sha> # v1.2.3`.
- Third-party actions in use today: `advanced-security/maven-dependency-submission-action@v3` in `dependabot-pom-scan.yml` (not yet SHA-pinned).
- Workflows should set minimal `permissions` and use `persist-credentials: false` on checkout when the job does not push.

## Avoid over-editing

Flag changes where the diff is disproportionate to the stated goal of the PR:

- Flag changes outside the scope of the PR: reformatting, reordered imports, renamed variables, rewritten code that already worked.
- Flag new abstractions, helpers or config that the change does not need.
- Flag comments that only restate the code.
- A small, focused diff is preferred over a "cleaner" larger one.
