# Copilot instructions: tiltaksgjennomforing-api

REST API backend for Nav's tiltaksgjennomføring system. It manages agreements (avtaler) for employment measures between Nav, employers and participants, and serves the external and internal frontends built from [navikt/tiltaksgjennomforing](https://github.com/navikt/tiltaksgjennomforing).

**Stack:** Java 21, Spring Boot 4, Maven, Lombok, Spring Data JPA with PostgreSQL (H2 locally and in tests), Flyway, Kafka with Avro, ShedLock, Unleash, token-support (TokenX and Entra ID). Deployed with Nais to `dev-fss` and `prod-fss`.

**Before committing**, run the same build as CI:

```bash
mvn package -B -Dsurefire.useFile=false
```

**[`AGENTS.md`](../AGENTS.md) is the main source** for access control, local setup, project structure, testing, patterns, domain glossary, Git workflow and boundaries. Read it before making changes. Review rules are in [`copilot-review-instructions.md`](copilot-review-instructions.md).
