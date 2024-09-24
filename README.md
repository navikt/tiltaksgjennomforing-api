Tiltaksgjennomføring API
===================================

For NAV-interne: Ta kontakt på Slack-kanal #arbeidsgiver-tiltak

For utviklere:
Bygges med Maven: `mvn install`

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
