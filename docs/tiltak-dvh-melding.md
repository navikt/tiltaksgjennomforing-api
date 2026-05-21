# Tiltak-DVH-melding

Beskrivelse av Kafka-topic `arbeidsgiver.tiltak-dvh-melding`.

Meldingene brukes til datadeling med datavarehuset (DVH) og inneholder hendelser knyttet til inngåtte tiltaksavtaler. Meldingene er serialisert med **Avro** og Avro-skjemaet finnes i `src/main/resources/avro/tiltaksgjennomforing.avsc`.

For spørsmål eller henvendelser kan rettes til [#arbeidsgiver-tiltak](https://nav-it.slack.com/archives/CCM9QUY3U) på Slack.

## Innhold

- [AvroTiltakHendelse](#avrotiltakhendelse)
- [Avtalestatus](#avtalestatus)
- [Hendelstype](#hendelstype)
- [Inkluderingstilskuddsutgift](#inkluderingstilskuddsutgift)
- [InkluderingstilskuddsutgiftType](#inkluderingstilskuddsutgifttype)
- [LonnstilskuddFormaal](#lonnstilskuddformaal)
- [MaalKategori](#maalkategori)
- [StillingType](#stillingtype)
- [TilskuddstrinnRecord](#tilskuddstrinnrecord)
- [TiltakKodeArena](#tiltakkodearena)
- [Tiltakstype](#tiltakstype)

## AvroTiltakHendelse

| Felt                          | Format                    | Beskrivelse                                                                                                                                                                                                         |
|-------------------------------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **meldingId**                 | `string` (UUID)           | Unik ID for meldingen.                                                                                                                                                                                              |
| **tidspunkt**                 | `datetime`                | Tidspunkt for når meldingen ble opprettet.                                                                                                                                                                          |
| **hendelseType**              | `string`                  | Hendelsen som trigget meldingen. Se [Hendelstype](#hendelstype).                                                                                                                                                    |
| **utfortAv**                  | `string`                  | NAV-ident på den som utførte handlingen som trigget hendelsen. Kan være `SYSTEM` eller `ARENA` for systemutløste hendelser.                                                                                         |
| **avtaleId**                  | `string` (UUID)           | ID for avtalen.                                                                                                                                                                                                     |
| **avtaleInnholdId**           | `string` (UUID)           | ID for gjeldende versjon av avtaleinnholdet da hendelsen inntraff.                                                                                                                                                  |
| **journalpostId**             | `string` \| `null`        | Journalpost-ID for det journalførte avtaledokumentet.                                                                                                                                                               |
| **tiltakstype**               | `string`                  | Type tiltak. Se [Tiltakstype](#tiltakstype).                                                                                                                                                                        |
| **tiltakskodeArena**          | `string` \| `null`        | Tilsvarende kode i Arena-fagsystemet. Se [TiltakKodeArena](#tiltakkodearena).                                                                                                                                       |
| **tiltakStatus**              | `string`                  | Avtalens status. Se [Avtalestatus](#avtalestatus).                                                                                                                                                                  |
| **deltakerFnr**               | `string`                  | Fødsels- eller D-nummer til deltaker (11 siffer).                                                                                                                                                                   |
| **bedriftNr**                 | `string`                  | Virksomhetsnummer til arbeidsgiveren (9 siffer). Se [brreg.no](https://www.brreg.no/bedrift/underenhet/).                                                                                                           |
| **harFamilietilknytning**     | `boolean` \| `null`       | `true` dersom deltaker og arbeidsgiver har en familietilknytning.                                                                                                                                                   |
| **veilederNavIdent**          | `string`                  | NAV-ident for veilederen som er ansvarlig for avtalen.                                                                                                                                                              |
| **startDato**                 | `date`                    | Startdato for tiltaket.                                                                                                                                                                                             |
| **sluttDato**                 | `date`                    | Sluttdato for tiltaket.                                                                                                                                                                                             |
| **stillingprosent**           | `number` \| `null`        | Stillingsprosent i heltall. Eks: 80 er 80%.                                                                                                                                                                         |
| **antallDagerPerUke**         | `number` \| `null`        | Antall dager per uke deltaker er på tiltaket.                                                                                                                                                                       |
| **maal**                      | `string` \| `null`        | **Ikke i bruk.**                                                                                                                                                                                                    |
| **arbeidstreningsMaal**       | `array`                   | Liste av målkategorier for arbeidstrening. Se [MaalKategori](#maalkategori). Kun relevant for tiltakstype `ARBEIDSTRENING`.                                                                                         |
| **stillingstype**             | `string` \| `null`        | Stillingstype. Se [StillingType](#stillingtype).                                                                                                                                                                    |
| **stillingstittel**           | `string` \| `null`        | Stillingstittel til deltaker.                                                                                                                                                                                       |
| **stillingStyrk08**           | `number` \| `null`        | Stilling til deltaker som STYRK-08 (Standard for yrkesklassifisering).                                                                                                                                              |
| **stillingKonseptId**         | `number` \| `null`        | Konsept-ID for stillingen fra Pam-ontologi.                                                                                                                                                                         |
| **lonnstilskuddProsent**      | `number` \| `null`        | Basis-lønnstilskuddsprosenten satt av veileder, i heltall. Eks: 52 er 52%. Dette er startprosenten og endres ikke ved eventuell reduksjon over tid. For faktisk prosent per periode, se `tilskuddstrinn[].prosent`. |
| **manedslonn**                | `number` \| `null`        | Deltakers månedslønn i hele NOK.                                                                                                                                                                                    |
| **feriepengesats**            | `number` \| `null`        | Feriepengesats oppgitt som desimaltall mellom 0 og 1. Eks: 0.12 er 12%.                                                                                                                                             |
| **feriepengerBelop**          | `number` \| `null`        | Feriepenger oppgitt i hele NOK.                                                                                                                                                                                     |
| **arbeidsgiveravgift**        | `number` \| `null`        | Arbeidsgiveravgift oppgitt som desimaltall mellom 0 og 1. Eks: 0.141 er 14.1%.                                                                                                                                      |
| **arbeidsgiveravgiftBelop**   | `number` \| `null`        | Arbeidsgiveravgift oppgitt i hele NOK.                                                                                                                                                                              |
| **otpSats**                   | `number` \| `null`        | Obligatorisk tjenestepensjon (OTP) oppgitt som desimaltall mellom 0 og 1. Eks: 0.02 er 2%.                                                                                                                          |
| **otpBelop**                  | `number` \| `null`        | Obligatorisk tjenestepensjon (OTP) oppgitt i hele NOK.                                                                                                                                                              |
| **sumLonnsutgifter**          | `number` \| `null`        | Sum av månedlige lønnsutgifter for arbeidsgiver (månedslønn, feriepenger, OTP, arbeidsgiveravgift) i hele NOK.                                                                                                      |
| **sumLonnstilskudd**          | `number` \| `null`        | Sum av månedlig lønnstilskudd beregnet med basis-lønnstilskuddsprosenten i hele NOK. Reflekterer ikke eventuell reduksjon over tid — for faktisk beløp per periode, se `tilskuddstrinn[].belopPerMnd`.              |
| **sumLonnstilskuddRedusert**  | `number` \| `null`        | **Ikke i bruk** — alltid `null`. Bruk heller `tilskuddstrinn`.                                                                                                                                                      |
| **datoForRedusertProsent**    | `date` \| `null`          | **Ikke i bruk** — alltid `null`. Bruk heller `tilskuddstrinn`.                                                                                                                                                      |
| **godkjentPaVegneAv**         | `boolean`                 | `true` dersom veileder godkjente avtalen på vegne av deltaker.                                                                                                                                                      |
| **ikkeBankId**                | `boolean`                 | `true` dersom årsaken til `godkjentPaVegneAv` er at deltaker mangler BankID.                                                                                                                                        |
| **reservert**                 | `boolean`                 | `true` dersom årsaken til `godkjentPaVegneAv` er at deltaker er reservert mot digital kommunikasjon.                                                                                                                |
| **digitalKompetanse**         | `boolean`                 | `true` dersom årsaken til `godkjentPaVegneAv` er at deltaker mangler digital kompetanse.                                                                                                                            |
| **arenaMigreringDeltaker**    | `boolean`                 | `true` dersom årsaken til `godkjentPaVegneAv` er Arena-migrering av deltaker.                                                                                                                                       |
| **godkjentAvDeltaker**        | `datetime`                | Tidspunkt for når deltaker godkjente avtalen.                                                                                                                                                                       |
| **godkjentAvArbeidsgiver**    | `datetime`                | Tidspunkt for når arbeidsgiver godkjente avtalen.                                                                                                                                                                   |
| **godkjentAvVeileder**        | `datetime`                | Tidspunkt for når veileder godkjente avtalen.                                                                                                                                                                       |
| **godkjentAvBeslutter**       | `datetime` \| `null`      | Tidspunkt for når beslutter godkjente avtalen. Gjelder kun for tiltakstyper med tilskuddsperioder.                                                                                                                  |
| **avtaleInngaatt**            | `datetime`                | Tidspunkt for avtaleinngåelse — når alle parter har godkjent.                                                                                                                                                       |
| **enhetOppfolging**           | `string` \| `null`        | Enhetsnummer (4 siffer) for enheten som følger opp deltakeren.                                                                                                                                                      |
| **enhetGeografisk**           | `string` \| `null`        | Enhetsnummer (4 siffer) for enheten som er geografisk tilknyttet deltakerens bostedsadresse.                                                                                                                        |
| **opprettetAvArbeidsgiver**   | `boolean`                 | `true` dersom avtalen ble opprettet av arbeidsgiver.                                                                                                                                                                |
| **annullertTidspunkt**        | `datetime` \| `null`      | Tidspunkt for annullering av avtalen.                                                                                                                                                                               |
| **annullertGrunn**            | `string` \| `null`        | Begrunnelse for annullering. Kan være fritekst eller en av: `Feilregistrering`, `Begynt i arbeid`, `Fått tilbud om annet tiltak`, `Syk`, `Ikke møtt`.                                                               |
| **master**                    | `boolean`                 | `true` for avtaletypene `SOMMERJOBB`, `MIDLERTIDIG_LONNSTILSKUDD` og `VARIG_LONNSTILSKUDD`, der tiltaksgjennomføring er kildesystem (master) for avtaledata.                                                        |
| **forkortetGrunn**            | `string` \| `null`        | Begrunnelse for forkorting av avtalen. Kan være: `Begynt i arbeid`, `Fått tilbud om annet tiltak`, `Syk`, `Ikke møtt`, `Fullført`. Kun satt ved hendelsetype `FORKORTET`.                                           |
| **avtaleNr**                  | `number` \| `null`        | Løpenummer for avtalen. Nyttig for enklere oppslag i avtaleløsningen.                                                                                                                                               |
| **mentorTimelonn**            | `number` \| `null`        | Timelønn til mentor i hele NOK. Kun relevant for tiltakstype `MENTOR`.                                                                                                                                              |
| **mentorAntallTimer**         | `number` \| `null`        | Antall mentortimer per måned. Kun relevant for tiltakstype `MENTOR`.                                                                                                                                                |
| **lonnstilskuddFormaal**      | `string` \| `null`        | Formålet med lønnstilskuddet. Se [LonnstilskuddFormaal](#lonnstilskuddformaal). Kun relevant for lønnstilskuddavtaler.                                                                                              |
| **inkluderingstilskuddsutgift** | `array`                 | Liste av inkluderingstilskuddsutgifter. Se [Inkluderingstilskuddsutgift](#inkluderingstilskuddsutgift). Kun relevant for tiltakstype `INKLUDERINGSTILSKUDD`.                                                        |
| **tilskuddstrinn**            | `array`                   | Liste av tilskuddstrinn per periode. Se [TilskuddstrinnRecord](#tilskuddstrinnrecord). Kun relevant for lønnstilskuddavtaler.                                                                                       |

## Avtalestatus

| Status                | Beskrivelse                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **KLAR_FOR_OPPSTART** | Avtalen er inngått, men har startdato fremover i tid.                        |
| **GJENNOMFØRES**      | Avtalen gjennomføres — deltaker er aktiv på tiltaket.                        |
| **AVSLUTTET**         | Avtalen har passert sluttdato og deltaker er ikke lenger på tiltaket.        |
| **ANNULLERT**         | Avtalen er annullert av veileder med tilhørende annulleringsgrunn.           |

## Hendelstype

| Type              | Beskrivelse                                                                              |
|-------------------|------------------------------------------------------------------------------------------|
| **INNGÅTT**       | Avtalen er inngått — alle parter har godkjent.                                           |
| **STATUSENDRING** | Avtalens status har endret seg automatisk (f.eks. fra `KLAR_FOR_OPPSTART` til `GJENNOMFØRES`). |
| **ENDRET**        | Innholdet i avtalen er endret etter at avtalen er inngått.                               |
| **ANNULLERT**     | Avtalen er annullert.                                                                    |
| **FORKORTET**     | Avtalens sluttdato er satt til en tidligere dato enn opprinnelig.                        |
| **FORLENGET**     | Avtalens sluttdato er satt til en senere dato enn opprinnelig.                           |
| **MIGRERING**     | Avtalen er migrert fra et annet system.                                                  |
| **PATCHING**      | Retting av feil i data på en tidligere melding.                                          |

## Inkluderingstilskuddsutgift

| Felt      | Format             | Beskrivelse                                                         |
|-----------|--------------------|---------------------------------------------------------------------|
| **belop** | `number` \| `null` | Beløp for utgiftsposten i hele NOK.                                 |
| **type**  | `string` \| `null` | Type utgift. Se [InkluderingstilskuddsutgiftType](#inkluderingstilskuddsutgifttype). |

## InkluderingstilskuddsutgiftType

| Type                              | Beskrivelse                           |
|-----------------------------------|---------------------------------------|
| **TILRETTELEGGINGSBEHOV**         | Tilretteleggingsbehov                 |
| **TILTAKSPLASS**                  | Tiltaksplass                          |
| **UTSTYR**                        | Utstyr                                |
| **PROGRAMVARE**                   | Programvare                           |
| **ARBEIDSHJELPEMIDLER**           | Arbeidshjelpemidler                   |
| **OPPLAERING**                    | Opplæring                             |
| **FORSIKRING_LISENS_SERTIFISERING** | Forsikring, lisens eller sertifisering |

## LonnstilskuddFormaal

| Type              | Beskrivelse                                           |
|-------------------|-------------------------------------------------------|
| **SKAFFE_ARBEID** | Lønnstilskuddet er ment å hjelpe deltaker å skaffe seg arbeid. |
| **BEHOLDE_ARBEID**| Lønnstilskuddet er ment å hjelpe deltaker å beholde eksisterende arbeid. |

## MaalKategori

| Kategori                          | Beskrivelse                        |
|-----------------------------------|------------------------------------|
| **FAA_JOBB_I_BEDRIFTEN**          | Få jobb i bedriften                |
| **ARBEIDSERFARING**               | Arbeidserfaring                    |
| **UTPROVING**                     | Utprøving                          |
| **SPRAAKOPPLAERING**              | Språkopplæring                     |
| **OPPNAA_FAGBREV_KOMPETANSEBEVIS**| Oppnå fagbrev/kompetansebevis      |
| **ANNET**                         | Annet                              |

## StillingType

| Type            | Beskrivelse      |
|-----------------|------------------|
| **FAST**        | Fast stilling    |
| **MIDLERTIDIG** | Midlertidig stilling |

## TilskuddstrinnRecord

| Felt           | Format             | Beskrivelse                                                              |
|----------------|--------------------|--------------------------------------------------------------------------|
| **start**      | `date` \| `null`   | Startdato for tilskuddstrinnet.                                          |
| **slutt**      | `date` \| `null`   | Sluttdato for tilskuddstrinnet.                                          |
| **prosent**    | `number` \| `null` | Lønnstilskuddsprosent for dette trinnet i heltall. Eks: 60 er 60%.      |
| **belopPerMnd**| `number` \| `null` | Beløp per måned for dette trinnet i hele NOK.                            |

## TiltakKodeArena

| Kode         | Tiltakstype                   |
|--------------|-------------------------------|
| **ARBTREN**  | Arbeidstrening                |
| **MIDLONTIL**| Midlertidig lønnstilskudd     |
| **VARLONTIL**| Varig lønnstilskudd           |
| **MENTOR**   | Mentor                        |
| **INKLUTILS**| Inkluderingstilskudd          |
| **VATIAROR** | Varig tilrettelagt arbeid i ordinær virksomhet (VTAO) |

## Tiltakstype

| Type                        | Beskrivelse                                     |
|-----------------------------|-------------------------------------------------|
| **ARBEIDSTRENING**          | Arbeidstrening                                  |
| **MIDLERTIDIG_LONNSTILSKUDD** | Midlertidig lønnstilskudd                     |
| **VARIG_LONNSTILSKUDD**     | Varig lønnstilskudd                             |
| **SOMMERJOBB**              | Sommerjobb                                      |
| **MENTOR**                  | Mentor                                          |
| **INKLUDERINGSTILSKUDD**    | Inkluderingstilskudd                            |
| **VTAO**                    | Varig tilrettelagt arbeid i ordinær virksomhet  |
| **FIREARIG_LONNSTILSKUDD**  | Fireårig lønnstilskudd                          |
