# AGENTS.md: navikt/tiltaksgjennomforing-api

## Introduction

REST API backend for Nav's tiltaksgjennomføring system. It manages agreements (`Avtale`) for employment measures (tiltak) between Nav, an employer (arbeidsgiver) and a participant (deltaker), and optionally a mentor. It covers creation, filling in, approval (godkjenning), subsidy periods (tilskuddsperioder), shortening and extending, cancellation, notifications and data sharing with other systems.

Owned by team tiltak (`@navikt/team-tiltak`, namespace `arbeidsgiver`). Runs on-prem in `dev-fss` and `prod-fss` (`nais/`).

### Consumers

One frontend codebase, [navikt/tiltaksgjennomforing](https://github.com/navikt/tiltaksgjennomforing), is deployed as two apps in `prod-gcp`. Both are allowed in by `accessPolicy.inbound` in `nais/prod-fss.yaml`:

| App | Users | Login / token issuer | Roles (`Avtalerolle`) |
|---|---|---|---|
| `tiltaksgjennomforing` (external) | Citizens and employers | ID-porten → TokenX (`tokenx`) | `DELTAKER`, `ARBEIDSGIVER`, `MENTOR` |
| `tiltaksgjennomforing-intern` (internal) | Nav employees | Entra ID (`aad`) | `VEILEDER`, `BESLUTTER` |

The frontend calls this API through its own Express server (backend for frontend). The server exchanges the user's token for an on-behalf-of token: Entra ID for the internal app, TokenX for the external app. The API never receives the browser's login token directly. The frontend's labs instance calls `tiltaksgjennomforing-api-labs` (see Setup & Local Development).

The frontend depends on this API in several places. Its TypeScript types in `src/types/` mirror our DTOs (`avtale/transportlag/AvtaleDTO`, `InnloggetBruker` and others), and it shows a message per `Feilkode`. Its feature toggle names must exist in `FeatureToggle`. See Patterns and Boundaries.

Other inbound callers: `min-side-arbeidsgiver` (namespace `fager`) and `tiltak-proxy` (marked for removal in the manifest).

### Integrations

- **Outbound HTTP:** POAO-tilgang (Nav employee access), PDL (person data, via `tiltak-persondata`), arbeidsgiver-altinn-tilganger (employer access), Norg2, EREG, veilarb (oppfølging), KRR (digdir-krr-proxy), kontoregister, Oppgave (Gosys tasks), entraproxy, Arena ORDS, Unleash and `tiltak-pdfgen` (PDF). URLs are in `application-{dev,prod}-fss.yaml`.
- **Kafka produce:** subsidy periods, avtale events, SMS, data warehouse (Avro), audit. Topic names are constants in `infrastruktur/kafka/Topics.java`.
- **Kafka consume:** `TILTAK_VARSEL`, `REFUSJON_ENDRET_STATUS` (from tiltak-refusjon) and Arena topics (configured in `tiltaksgjennomforing.arena.kafka.*`).

## Access Control

The API is the security boundary. All access control is enforced here, never in the frontend.

1. **Token validation:** `@EnableJwtTokenValidation` in `TiltaksgjennomforingApplication`. Controllers use `@Protected` or `@ProtectedWithClaims(issuer = ...)`. `FeatureToggleController` is the only class excluded from token validation.
2. **Issuers** (`no.nav.security.jwt.issuer` in `application-{dev,prod}-fss.yaml`):
   - `tokenx`: external users (deltaker, arbeidsgiver, mentor). The user identifier is the `pid` claim.
   - `aad`: Nav employees (veileder, beslutter). The user identifier is the `NAVident` claim. AD groups come from the `groups` claim.
   - `azure-access-token`: team members calling admin endpoints (`AdminController`, `ArenaAdminController`, `TilskuddsperiodeAdminController` and others). See README for how to get a token.
   - `system`: service-to-service calls (`InternalAvtaleController`).
3. **Role selection:** the client sends its role in the `innlogget-part` cookie. `InnloggingService.hentAvtalepart(Avtalerolle)` only accepts valid issuer/role combinations (otherwise `Feilkode.UGYLDIG_KOMBINASJON_AV_ISSUER_OG_ROLLE`). `BESLUTTER` also requires the beslutter AD group (`AdGruppeProperties`). Never trust the cookie without going through `InnloggingService`.
4. **Per-agreement access:** each `Avtalepart` subclass (`Deltaker`, `Arbeidsgiver`, `Mentor`, `Veileder`, `Beslutter`) implements `harTilgangTilAvtale(...)`, which returns `Tilgang` (sealed: `Tillat` / `Avvis`). Veileder and beslutter use POAO-tilgang (`autorisasjon/abac/TilgangskontrollService`). Arbeidsgiver uses Altinn (`autorisasjon/altinntilgangsstyring/AltinnTilgangsstyringService`). Field masking per role is done in `Avtalepart.maskerFelterForAvtalepart`.
5. **Strengt fortrolig / fortrolig adresse:** handled via AD groups (`ad-grupper.*`) and Altinn address-protection rights.

Frontend logic that hides elements by role is not access control. Every new end-user endpoint must resolve the caller through `InnloggingService.hentAvtalepart(...)` and check `Tilgang` for the specific resource. System and admin endpoints use other checks (see Auth and HTTP clients under Patterns).

## Setup & Local Development

Requirements: JDK 21 and Maven. All dependencies resolve from public repositories or Nav's GitHub package mirror (`github-package-registry-mirror.gc.nav.no`) declared in `pom.xml`. No `settings.xml` or token is needed.

The production entry point `TiltaksgjennomforingApplication` refuses to start without the `MILJO` environment variable, which selects the Spring profile.

| Mode | How to start | Profiles | Notes |
|---|---|---|---|
| Local (default) | Run `LokalTiltaksgjennomforingApplication` (in `src/test/java`) from the IDE | `local`, `wiremock`, `testdata` | H2 in memory (`MODE=PostgreSQL`), WireMock for external services, generated test data, Kafka disabled. H2 console at `/tiltaksgjennomforing-api/h2-console`. |
| Docker Compose | `docker compose up`, then run `DockerComposeTiltaksgjennomforingApplication` | `dockercompose`, `testdata`, `wiremock` | Real Kafka, Schema Registry and Postgres (port 6432) from `docker-compose.yml`. |
| Labs | Workflow `bygg-og-deploy-labs.yml` (weekday schedule or manual) | `local`, `wiremock`, `testdata` | Builds `DockerfileLabs`, which runs the local launcher with test classes on the loader path. Deployed to `dev-gcp` with `nais/dev-gcp-labs.yaml`. |

Local login uses [tiltak-fakelogin](https://github.com/navikt/tiltak-fakelogin) as issuer for `aad` and `tokenx` (`FAKE_LOGIN_URI`, see `src/test/resources/config/application-local.yaml`).

Local feature toggles use `FakeUnleash`, controlled by the `features` request header (see README).

Example admin calls: `src/test/resources/admin-kall.http`.

## Build & Test Commands

```bash
mvn package -B -Dsurefire.useFile=false   # What CI runs (bygg.yml): compile, Avro codegen, all tests, jar + SBOM
mvn test                                   # Tests only
mvn test -Dtest=AvtaleTest                 # Single test class
```

Run `mvn package -B -Dsurefire.useFile=false` before every commit. CI runs exactly this on every pull request (`bygg-pr.yml`). CodeQL (`codeql.yml`) also runs on PRs to `master`.

`target/generated-sources/avro/` is generated from `src/main/resources/avro/*.avsc` during `generate-sources`. Do not edit generated classes.

## Project Structure

```
.github/workflows/   CI/CD: bygg.yml (shared build), bygg-pr.yml, bygg-og-deploy.yml (master → dev-fss → prod-fss),
                     bygg-og-deploy-labs.yml, codeql.yml, dependabot-pom-scan.yml
nais/                Environment-specific manifests (no shared templates):
                       dev-fss.yaml, prod-fss.yaml   the application per cluster
                       dev-gcp-labs.yaml             labs instance with mocks
                       unleash-apitoken.yml          Unleash API token; {{ unleash-environment }} set at deploy
                       alerterator.yaml              PrometheusRule alerts (prod only)
                     {{image}} is filled in by nais/deploy at deploy time.
live-templates/      IntelliJ live templates (team logs logger)
src/main/java/no/nav/tag/tiltaksgjennomforing/
  avtale/            Core domain: Avtale, AvtaleInnhold, Avtalepart subclasses, strategies, AvtaleController,
                     events/ (domain events), jobber/ (scheduled jobs), service/, admin/, startOgSluttDatoStrategy/
  autorisasjon/      Login, token utils, issuer/role resolution, POAO-tilgang (abac/), Altinn
  tilskuddsperiode/  Subsidy period Kafka messages and beregning/ (calculation)
  datadeling/        Outbound avtale events (outbox + Kafka) and AvtaleMelding JSON contract
  datavarehus/       Avro events to the data warehouse
  arena/             Migration and sync of agreements from Arena (Kafka consumer, ORDS client, jobs)
  varsel/            In-app notifications, SMS and Gosys tasks
  featuretoggles/    Unleash integration, FeatureToggle enum, FakeUnleash
  infrastruktur/     Kafka config, auditing (@AuditLogging), cache, database, filters
  exceptions/        Feilkode enum, FeilkodeException and ApiExceptionHandler
  enhet/, persondata/, orgenhet/, oppfolging/, okonomi/, journalfoering/, brev/, pdfgen/, satser/,
  kodeverk/, sporingslogg/, metrikker/, logging/, utils/, dev/ (dev-only endpoints)
src/main/resources/
  config/            application.yaml + application-{dev,prod}-fss.yaml
  db/migration/      Flyway: common/ (all vendors) and postgresql/ (Postgres-only)
  avro/              Avro schemas
  scripts/           One-off SQL scripts, not run by Flyway
src/test/java/...    Tests, plus local launchers, TestData, WireMock server and test data loaders
src/test/resources/  application-{test,local,dockercompose} config, WireMock mappings/, test keys
```

## Testing

- JUnit 5, AssertJ and Mockito (`@MockitoBean` for Spring beans). Test classes are named `*Test.java` and mirror the main package.
- Test method names use snake_case, for example `hentInnloggetBruker__er_nav_ansatt_og_beslutter` in older tests and pure snake_case in new tests.
- **Unit tests** for domain logic need no Spring context (`AvtaleTest`, `VeilederTest`, `ArbeidsgiverTest`).
- **Integration tests:** `@SpringBootTest`, `@ActiveProfiles({Miljø.TEST, Miljø.WIREMOCK})` when external calls are needed, `@EmbeddedKafka` for Kafka, and `@DirtiesContext`.
- H2 in-memory DB is configured in `src/test/resources/config/application-test.yaml`. Migrations in `db/migration/common/` run on both H2 (PostgreSQL mode) and PostgreSQL and must support both. Migrations in `db/migration/postgresql/` run only on PostgreSQL and are not exercised by H2 tests.
- External services are mocked with WireMock (`IntegrasjonerMockServer`, JSON stubs in `src/test/resources/mappings/`).
- Fixtures: `TestData` (domain objects) and `TestDataGenerator`. Reuse them instead of building new fixtures.
- Use `utils/Now` for current time in production code, so tests can control the clock.
- Thin or no test coverage: `arena/` (one test), `datadeling/`, `exceptions/`, `satser/`, `sporingslogg/`, `pdfgen/`, `kodeverk/`, `dev/`. Add tests when you change these areas.

## Patterns

Use the existing solution for each concern. Do not introduce a parallel variant.

### Domain and business rules

- Business rules live on `Avtale` and the `Avtalepart` subclasses, not in controllers. Controllers resolve the caller, load the `Avtale`, call a domain method and save.
- Per-tiltakstype rules live in `AvtaleInnholdStrategy` implementations, chosen by `AvtaleInnholdStrategyFactory` (switch on `Tiltakstype`). Start and end date rules live in `avtale/startOgSluttDatoStrategy/`.
- Domain validation errors: `throw new FeilkodeException(Feilkode.X)`. Add new codes to `exceptions/Feilkode.java`. `ApiExceptionHandler` returns the code in the `feilkode` response header, and the frontend shows a message per code. A new or removed code therefore needs a matching change in the frontend.
- Input validation happens in the domain: domain methods check their input and throw `FeilkodeException`, and `Avtale.felterSomIkkeErFyltUt()` decides which fields are missing before approval. Bean Validation (`@Valid`) is not used on request bodies. Never rely on the frontend for validation.
- `AvtaleInnhold.startDato` and `sluttDato` are nullable for agreements still being filled in (`PÅBEGYNT`).

### Database and migrations

- Spring Data JPA. Prefer derived query methods or JPQL in `@Query` with bound parameters. Never concatenate input into JPQL/SQL.
- `AvtaleRepository.sokEtterAvtale` has a value query and a count query. Keep the filter predicates identical in both.
- Flyway migrations go in `src/main/resources/db/migration/common/` as `V{n}__{description}.sql` (use `V{n}_{m}__` for a follow-up). Find the highest existing number first. Postgres-only SQL goes in `db/migration/postgresql/`. Never edit a migration that has been merged.
- `Avtale.avtaleNr` is generated by the database. Do not set it in code.
- JSON columns use Jackson 3 (`tools.jackson.databind`); `hibernate.type.json_format_mapper: jackson3` in `application.yaml` is required.

### Events and Kafka

- `Avtale` extends `AbstractAggregateRoot`. State changes call `registerEvent(new SomeEvent(...))` with a class from `avtale/events/`. Events are published when the repository saves the aggregate.
- Listeners use `@EventListener` (varsel, sporingslogg, datavarehus, metrikker, datadeling). Add a new listener method instead of calling side effects from the domain.
- Outbound avtale events use an outbox: `AvtaleHendelseLytter` stores an `AvtaleMeldingEntitet` in the same transaction, and `AvtaleMeldingKafkaProdusent` sends it with `@TransactionalEventListener` to `AVTALE_HENDELSE` and `AVTALE_HENDELSE_COMPACT`.
- `AvtaleMelding` is a public JSON contract, also exposed as a JSON schema by `InternalAvtaleHendelseController`. Treat field changes as breaking for consumers.
- Data warehouse messages use the Avro schema in `src/main/resources/avro/` and a deterministic content hash as key (`AvroTiltakHendelseFabrikk.beregnNokkel`).
- Add new topics as constants in `Topics.java`.

### Scheduled jobs

- Jobs live in `avtale/jobber/` (and `arena/job/`). Use `@Scheduled` together with `@SchedulerLock` (ShedLock), because several pods run in parallel.
- Batch work pages with a UUID cursor and `Limit.of(500)` ordered by id, and loops until a batch is smaller than the limit (see `AvtalestatusJobbService`).

### Auth and HTTP clients

- New endpoints: pick the check that matches the caller.
  - End-user agreement endpoints (`tokenx`/`aad`): `@Protected`, resolve the party with `InnloggingService.hentAvtalepart(...)` and check `Tilgang`. `hentAvtalepart` only accepts tokenx/aad role combinations.
  - System endpoints (`system` issuer): `@ProtectedWithClaims(issuer = "system")` plus `InnloggingService.validerSystembruker()` (see `journalfoering/InternalAvtaleController`).
  - Admin endpoints (`azure-access-token`): `@ProtectedWithClaims` with issuer and AD group claims (see `avtale/admin/AdminController`). These do not use `hentAvtalepart`.
- Mark endpoints that return personal data with `@AuditLogging` (`infrastruktur/auditing/`).
- Outbound HTTP uses `RestTemplate` (one `RestClient`, in `PoaoTilgangHttpRetryKlient`), with tokens from `token-client-spring` registrations (`no.nav.security.jwt.client.registration`). Helpers are in `infrastruktur/RestTemplateBuilders.java`. Copy the setup of an existing client in the same area.

### Feature toggles

- Declare toggles in `featuretoggles/FeatureToggle.java`. Check them with `FeatureToggleService.isEnabled(FeatureToggle.X)`. Locally and in tests, `FakeUnleash` is used.
- The frontend fetches toggles from `GET /feature?feature=...` (`FeatureToggleController`, no login required). `FeatureToggleConverter` throws on unknown toggle names, so the whole request fails, not just the unknown toggle. A toggle the frontend uses must exist here first, and it must not be removed until the frontend has stopped asking for it.
- `FeatureToggleService.isTiltakSkrivebeskyttet(Tiltakstype)` makes agreements of some measure types read-only during migration. The frontend mirrors this with `useMigreringSkrivebeskyttet`.
- Remove the toggle and its dead branch once it is fully rolled out, in step with the frontend.

### Logging

- Ordinary logs (`@Slf4j`) must never contain fødselsnummer, names, addresses or tokens. Log IDs such as `avtaleId` instead.
- Personal data that must be logged goes through `logging/TeamLogs` (marker `TEAM_LOGS`, shipped to team logs only).

## Domain Glossary

| Term | Meaning | Type |
|---|---|---|
| Avtale | Agreement for one participant in one tiltak with one employer | `avtale/Avtale` |
| AvtaleInnhold | Versioned content of an agreement (dates, salary, goals and more) | `avtale/AvtaleInnhold` |
| Tiltakstype | Kind of employment measure: arbeidstrening, lønnstilskudd (midlertidig, varig, fireårig), mentor, inkluderingstilskudd, sommerjobb, VTAO | `avtale/Tiltakstype` |
| Avtalepart / Avtalerolle | A party to the agreement and its role | `avtale/Avtalepart`, `avtale/Avtalerolle` |
| Deltaker | Participant (citizen) | `avtale/Deltaker` |
| Arbeidsgiver | Employer representative, authorized through Altinn | `avtale/Arbeidsgiver` |
| Veileder | Nav case worker who owns the agreement | `avtale/Veileder` |
| Beslutter | Nav employee who approves subsidy periods | `avtale/Beslutter` |
| Mentor | Person supporting the participant at the workplace | `avtale/Mentor` |
| Godkjenning | Approval by a party; all parties must approve before the agreement is inngått | methods on `Avtalepart` |
| Godkjent på vegne av | Veileder approves on behalf of the participant and/or employer, with a stated reason | `avtale/GodkjentPaVegneGrunn`, `Veileder.godkjennForVeilederOgDeltaker(...)` and similar |
| Inngått | Agreement concluded (all approvals done) | `Avtale.erAvtaleInngått()` |
| Status | Derived agreement status: `PÅBEGYNT`, `MANGLER_GODKJENNING`, `KLAR_FOR_OPPSTART`, `GJENNOMFØRES`, `AVSLUTTET`, `ANNULLERT` | `avtale/Status` |
| Tilskuddsperiode | Subsidy period to be approved by a beslutter and later refunded | `avtale/TilskuddPeriode`, `TilskuddPeriodeStatus` |
| Avslå / returnere | Beslutter rejects a subsidy period and sends it back to the veileder with reasons (status `AVSLÅTT`) | `Beslutter.avslåTilskuddsperiode(...)`, `avtale/Avslagsårsak` |
| Refusjon | Refund of subsidy to the employer, handled by tiltak-refusjon | `tilskuddsperiode/RefusjonEndretStatusMelding` |
| Forkorte / forlenge | Shorten / extend an agreement | `avtale/ForkortAvtale`, `avtale/ForlengAvtale` |
| Annullere | Cancel an agreement | `avtale/AnnullertGrunn` |
| Fnr | Fødselsnummer (national identity number) | `avtale/Fnr` |
| BedriftNr | Organization number of the employer's business | `avtale/BedriftNr` |
| NavIdent | Nav employee ID | `avtale/NavIdent` |
| Enhet | Nav office unit | `enhet/` |
| Innsatsgruppe / Kvalifiseringsgruppe / Formidlingsgruppe | Follow-up classifications of the participant from veilarb/Arena | `enhet/Innsatsgruppe`, `enhet/Kvalifiseringsgruppe`, `enhet/Formidlingsgruppe` |
| Arena | Nav's legacy case system; agreements are migrated from it | `arena/` |
| Varsel / SMS | In-app notification / text message to a party | `varsel/Varsel`, `varsel/Sms` |
| Sporingslogg | Access trail for an agreement | `sporingslogg/Sporingslogg` |
| Feilkode | Error code returned to the frontend | `exceptions/Feilkode` |

## Code Style

- Java 21, not Kotlin. Follow the conventions of the surrounding code.
- Domain names are Norwegian (often with æ/ø/å in class names), framework code is English. Keep this split.
- Lombok is used throughout (`@Data`, `@Value`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`). Do not write manual getters, setters or constructors that Lombok already provides.
- Use records or sealed interfaces for small value and result types (see `Tilgang`).
- **Minimal Editing:** change only what the task requires. Do not reformat, reorder imports, rename or refactor unrelated code. Do not add comments that restate the code.

## Git Workflow

- The main branch is `master`. Never commit or push directly to `master`; all changes go through a pull request.
- Branch names: `type/short-description`, for example `fix/forleng-avtale` or `chore/fjerne-toggle`.
- Commit messages: Conventional Commits in Norwegian, `type(scope): beskrivelse`, for example `fix(avtale): tillat forkorting uten innsatsgruppe`. Types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`.
- The commit message should include a short title, but should also have a body that explains more in details what has been changed and why it was changed. If the reason for the changes are unclear just from the context of the code, please ask for more information to make the message descriptive enough for future readers.
- Merging to `master` deploys to dev-fss and then prod-fss automatically.

## Boundaries

### ✅ Always

- Run `mvn package -B -Dsurefire.useFile=false` (the same build as CI) before committing.
- On end-user agreement endpoints (`tokenx`/`aad`), resolve the caller with `InnloggingService.hentAvtalepart(...)` and check `Tilgang`. System and admin endpoints use `validerSystembruker()` or issuer/group claims instead (see Auth and HTTP clients).
- Use bound parameters in all queries.
- Validate all external input (request bodies, query parameters, Kafka messages) in the API, even when the frontend already validates it.
- Write new Flyway migrations instead of editing existing ones.
- Follow the patterns in this file and reuse `TestData`.

### ⚠️ Ask First

- Changes in `autorisasjon/`, token validation, issuer configuration, `@Protected*` annotations or AD group config: this is the trust boundary.
- New or changed Flyway migrations (database schema).
- Changes to `AvtaleMelding`, Avro schemas, Kafka topics or message formats, and DTOs other systems depend on.
- Changes to the frontend contract: response DTOs (`avtale/transportlag/`, `InnloggetBruker`), adding or removing `Feilkode` values, and removing or renaming a `FeatureToggle`. These need a matching change in [navikt/tiltaksgjennomforing](https://github.com/navikt/tiltaksgjennomforing).
- New dependencies in `pom.xml` (versions go in `<properties>`).
- Changes to `nais/*.yaml` (access policy, ingresses, secrets, resources) or workflows.
- New admin endpoints or changes to existing ones.

### 🚫 Never

- Commit secrets, tokens or real personal data (use synthetic fødselsnummer in tests).
- Log fødselsnummer, names, addresses or tokens outside `TeamLogs`.
- Bypass or weaken access checks, or trust the `innlogget-part` cookie without `InnloggingService`.
- Skip input validation because the frontend validates.
- Edit generated Avro classes or merged migrations.
- Commit directly to `master`.
