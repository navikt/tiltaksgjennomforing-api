Tiltaksgjennomføring API
===================================

For NAV-interne: Ta kontakt på Slack-kanal #arbeidsgiver-tiltak

For utviklere:
Bygges med Maven: `mvn install`

## Admin

### Autentisering

Admin-grensesnittet krever JWT token fra Azure AD og er kun tilgjengelig for medlemmer av gruppen [team-tiltak](https://teamkatalog.nav.no/team/0150fd7c-df30-43ee-944e-b152d74c64d6).

1. Last ned Azure CLI: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli
2. Logg inn med Azure CLI: `az login`
3. Hent token: `az account get-access-token`
4. Kopier tokenet og legg det inn i Authorization-headeren med CURL eller en annen REST-klient

## Feature toggles

Alle feature-toggles finner du i [Unleash](https://team-tiltak-unleash-web.iap.nav.cloud.nais.io/projects/default).

Bruk [ModHeader](https://modheader.com/) for å sette feature-toggle header lokalt (eller i labs).

#### Skru på alle toggles

`features = enabled`

#### Skru av noen toggles 

Eksempel, skru på alle utenom arbeidstreningReadonly og vtaoTiltakToggle som er skrudd av:

`featues = enabled,!arbeidstreningReadonly,!vtaoTiltakToggle`

#### Skru av alle toggles

`features = disbaled (eller bare fjerne headeren)`

#### Skru på noen toggles

Eksempel, skru av alle utenom arbeidstreningReadonly og vtaoTiltakToggle som er skrudd på:

`featues = disabled,!arbeidstreningReadonly,!vtaoTiltakToggle`
