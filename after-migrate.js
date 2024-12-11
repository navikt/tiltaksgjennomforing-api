/*
 * Genrerer testdata for Arena-migrering fra som er definert her:
 * https://navno.sharepoint.com/:x:/r/sites/TAG-Teamtiltak/_layouts/15/Doc.aspx?sourcedoc=%7BB0D02180-2555-48CB-B4D2-4F01E8F472E4%7D&file=Arena-migrering.xlsx
 */

const fs = require('node:fs');
const crypto = require('node:crypto');

const fornavn = [
    'Aktuell',
    'Alvorlig',
    'Ansvarlig',
    'Berømt',
    'Betydelig',
    'Bevisst',
    'Bred',
    'Dum',
    'Dyp',
    'Ekkel',
    'Eksisterende',
    'Ekte',
    'Enkel',
    'Ensom',
    'Falsk',
    'Fast',
    'Felles',
    'Fersk',
    'Fjern',
    'Flau',
    'Følsom',
    'Forsiktig',
    'Fremmed',
    'Fryktelig',
    'Glatt',
    'Gravid',
    'Grunnleggende',
    'Heldig',
    'Hemmelig',
    'Hjelpsom',
    'Hyppig',
    'Imponerende',
    'Kjedelig',
    'Kul',
    'Langsom',
    'Lat',
    'Lav',
    'Lignende',
    'Løs',
    'Lovlig',
    'Lykkelig',
    'Lys',
    'Menneskelig',
    'Merkelig',
    'Midlertidig',
    'Mistenkelig',
    'Modig',
    'Mørk',
    'Morsom',
    'Motsatt',
    'Mulig',
    'Naturlig',
    'Nåværende',
    'Nødvendig',
    'Nøyaktig',
    'Nysgjerrig',
    'Nyttig',
    'Offentlig',
    'Opprinnelig',
    'Ordentlig',
    'Plutselig',
    'Rå',
    'Rask',
    'Regelmessig',
    'Ren',
    'Rettferdig',
    'Rimelig',
    'Rund',
    'Ryddig',
    'Sannsynlig',
    'Selvsikker',
    'Sint',
    'Skarp',
    'Skikkelig',
    'Skyldig',
    'Smal',
    'Søt',
    'Spennende',
    'Stille',
    'Stolt',
    'Stram',
    'Streng',
    'Stygg',
    'Sulten',
    'Sunn',
    'Synlig',
    'Tilgjengelig',
    'Tilstrekkelig',
    'Tung',
    'Tynn',
    'Uavhengig',
    'Ujevn',
    'Ulovlig',
    'Ulykkelig',
    'Umiddelbar',
    'Urettferdig',
    'Vellykket',
    'Vennlig',
    'Verdifull',
    'Vill',
    'Villig',
    'Voksen',
    'Ærlig',
    'Åpen',
    'Åpenbar',
];

const etternavn = [
    'Avstand',
    'Bakgrunn',
    'Ball',
    'Bilde',
    'Blod',
    'Bok',
    'Bokstav',
    'Bunn',
    'By',
    'Del',
    'Drøm',
    'Eksempel',
    'Eske',
    'Evne',
    'Familie',
    'Farge',
    'Feil',
    'Fest',
    'Flagg',
    'Flaske',
    'Fordel',
    'Forhold',
    'Form',
    'Forskjell',
    'Forslag',
    'Gang',
    'Grense',
    'Grunn',
    'Håp',
    'Humør',
    'Hus',
    'Idrett',
    'Jobb',
    'Kamp',
    'Kart',
    'Konge',
    'Konkurranse',
    'Kropp',
    'Lag',
    'Land',
    'Liv',
    'Lov',
    'Løype',
    'Luft',
    'Lyd',
    'Lys',
    'Mål',
    'Mat',
    'Måte',
    'Menneske',
    'Merke',
    'Miljø',
    'Møte',
    'Nøkkel',
    'Område',
    'Oppgave',
    'Ord',
    'Øyeblikk',
    'Pause',
    'Pose',
    'Pris',
    'Prøve',
    'Retning',
    'Sak',
    'Salg',
    'Samfunn',
    'Samtale',
    'Sang',
    'Seier',
    'Seng',
    'Side',
    'Situasjon',
    'Skade',
    'Skilt',
    'Skole',
    'Skritt',
    'Slutt',
    'Søvn',
    'Spill',
    'Spøk',
    'Spørsmål',
    'Sted',
    'Stemme',
    'Stjerne',
    'Støtte',
    'Stund',
    'Stykke',
    'Svar',
    'Sykdom',
    'Tall',
    'Tekst',
    'Tid',
    'Time',
    'Ting',
    'Utfordring',
    'Vær',
    'Valg',
    'Vei',
    'Venn',
    'Årsak',
];

const bedriftsnavn = [...fornavn, ...etternavn].map(navn => `${navn} AS`);

const statuser = [
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 1},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 5},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'BAKOVER', antall: 668},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'FREMOVER', antall: 7},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 10},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 10},
    {status: 'ANNULLERT', tiltakstatus: 'AVBRUTT', antall: 48},
    {status: 'ANNULLERT', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'FREMOVER', antall: 6},
    {status: 'ANNULLERT', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'BAKOVER', antall: 119},
    {status: 'ANNULLERT', tiltakstatus: 'AVLYST', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 1},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 4},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 752},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 2},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 54},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 41},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'TILBUD', tid: 'BAKOVER', antall: 20},
    {status: 'ANNULLERT', tiltakstatus: 'AVSLUTT', antall: 287},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'AKTUELL', tid: 'FREMOVER', antall: 2},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'FULLF', tid: 'FREMOVER', antall: 1},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 1},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'BAKOVER', antall: 2},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 21},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'IKKAKTUELL', tid: 'FREMOVER', antall: 3},
    {status: 'ANNULLERT', tiltakstatus: 'GJENNOMFOR', antall: 21},
    {status: 'AVBRUTT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'BAKOVER', antall: 278},
    {status: 'AVBRUTT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 3},
    {status: 'AVBRUTT', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 1},
    {status: 'AVBRUTT', tiltakstatus: 'AVBRUTT', antall: 8},
    {status: 'AVBRUTT', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'BAKOVER', antall: 7},
    {status: 'AVBRUTT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 2},
    {status: 'AVBRUTT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 209},
    {status: 'AVBRUTT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 18},
    {status: 'AVBRUTT', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 3},
    {status: 'AVBRUTT', tiltakstatus: 'AVSLUTT', antall: 77},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'AKTUELL', tid: 'FREMOVER', antall: 1},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 22},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 56},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'BAKOVER', antall: 4640},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'FREMOVER', antall: 12},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 76},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 15},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', deltakerstatus: 'TILBUD', tid: 'BAKOVER', antall: 1},
    {status: 'AVSLUTTET', tiltakstatus: 'AVBRUTT', antall: 649},
    {status: 'AVSLUTTET', tiltakstatus: 'AVLYST', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 2},
    {status: 'AVSLUTTET', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'BAKOVER', antall: 133},
    {status: 'AVSLUTTET', tiltakstatus: 'AVLYST', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 7},
    {status: 'AVSLUTTET', tiltakstatus: 'AVLYST', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 4},
    {status: 'AVSLUTTET', tiltakstatus: 'AVLYST', antall: 19},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 136},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'FREMOVER', antall: 1},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 42462},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 4},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 828},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 98},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', deltakerstatus: 'TILBUD', tid: 'BAKOVER', antall: 401},
    {status: 'AVSLUTTET', tiltakstatus: 'AVSLUTT', antall: 5491},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 1},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'FULLF', tid: 'FREMOVER', antall: 6},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 126},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'BAKOVER', antall: 72},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'IKKAKTUELL', tid: 'FREMOVER', antall: 3},
    {status: 'AVSLUTTET', tiltakstatus: 'GJENNOMFOR', antall: 21},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', deltakerstatus: 'AKTUELL', tid: 'FREMOVER', antall: 2},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'FREMOVER', antall: 6},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', deltakerstatus: 'GJENN_AVB', tid: 'BAKOVER', antall: 35},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', deltakerstatus: 'IKKAKTUELL', tid: 'FREMOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVBRUTT', antall: 15},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'BAKOVER', antall: 3},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', deltakerstatus: 'GJENN_AVL', tid: 'FREMOVER', antall: 5},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', deltakerstatus: 'IKKAKTUELL', tid: 'FREMOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVLYST', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'AKTUELL', tid: 'BAKOVER', antall: 7},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'BAKOVER', antall: 857},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'FULLF', tid: 'FREMOVER', antall: 18},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 39},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKAKTUELL', tid: 'BAKOVER', antall: 12},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'IKKEM', tid: 'BAKOVER', antall: 3},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', deltakerstatus: 'TILBUD', tid: 'BAKOVER', antall: 31},
    {status: 'GJENNOMFØRES', tiltakstatus: 'AVSLUTT', antall: 154},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'AKTUELL', tid: 'FREMOVER', antall: 9},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'FULLF', tid: 'FREMOVER', antall: 1},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'BAKOVER', antall: 79},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 3418},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'IKKAKTUELL', tid: 'FREMOVER', antall: 8},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'IKKEM', tid: 'FREMOVER', antall: 3},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'TILBUD', tid: 'FREMOVER', antall: 82},
    {status: 'GJENNOMFØRES', tiltakstatus: 'GJENNOMFOR', antall: 381},
    {status: 'KLAR_FOR_OPPSTART', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'GJENN', tid: 'FREMOVER', antall: 1},
    {status: 'KLAR_FOR_OPPSTART', tiltakstatus: 'GJENNOMFOR', deltakerstatus: 'TILBUD', tid: 'FREMOVER', antall: 23},
    {status: 'KLAR_FOR_OPPSTART', tiltakstatus: 'GJENNOMFOR', antall: 7}
];

const addRandomDays = (date, maxDays, minDays = 1) => new Date(date.getTime() + (Math.round((maxDays - minDays) * Math.random()) + minDays) * 24 * 60 * 60 * 1000);
const addRandomDaysFromNow = (maxDays, minDays = 1) => addRandomDays(new Date(), maxDays, minDays);
const randomDayBetween = (minDate, maxDate) => new Date(Math.round((maxDate.getTime() - minDate.getTime()) * Math.random()) + minDate.getTime());

const formatDato = (dato) => dato.substring(0, 10);
const generateFornavn = () => fornavn[Math.round(Math.random() * (fornavn.length - 1))];
const generateEtternavn = () => etternavn[Math.round(Math.random() * (etternavn.length - 1))];
const generateBedriftsnavn = () => bedriftsnavn[Math.round(Math.random() * (bedriftsnavn.length - 1))];
const generateBedriftnr = () => 900000000 + Math.round(Math.random() * 99999999);
const generateTlf = () => Math.random() > 0.5 ? 40000000 + Math.round(Math.random() * 9999999) : 90000000 + Math.round(Math.random() * 9999999);
const generateRandomSSN = () => {
    const getRandomInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

    const padZero = (num, size) => {
        let s = num + "";
        while (s.length < size) s = "0" + s;
        return s;
    };

    const generateDatePart = () => {
        const day = padZero(getRandomInt(1, 28), 2);
        const month = padZero(getRandomInt(1, 12), 2);
        const year = padZero(getRandomInt(0, 99), 2);
        return `${day}${month}${year}`;
    };

    const generateIndividualNumber = () => padZero(getRandomInt(0, 999), 3);

    const calculateIndividualAndControlParts = (datePart) => {
        const individualNumber = generateIndividualNumber();
        const weights1 = [3, 7, 6, 1, 8, 9, 4, 5, 2];
        const weights2 = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];

        const digits = datePart + individualNumber;
        const sum1 = digits.split('').reduce((sum, digit, index) => sum + digit * weights1[index], 0);
        let controlDigit1 = 11 - (sum1 % 11);
        if (controlDigit1 === 11) controlDigit1 = 0;

        const sum2 = (digits + controlDigit1).split('').reduce((sum, digit, index) => sum + digit * weights2[index], 0);
        let controlDigit2 = 11 - (sum2 % 11);
        if (controlDigit2 === 11) controlDigit2 = 0;

        if (isNaN(controlDigit1) || isNaN(controlDigit2) || controlDigit1 === 10 || controlDigit2 === 10) {
            return calculateIndividualAndControlParts(datePart);
        }

        return `${individualNumber}${controlDigit1}${controlDigit2}`;
    };

    const datePart = generateDatePart();
    const individualAndControlPart = calculateIndividualAndControlParts(datePart);

    return `${datePart}${individualAndControlPart}`;
}

const generateAvtale = (avtaleId, fnr, bedriftNr, datoFra, datoTil, status, annullertDato, avbruttDato) => {
    const avtaleInhholdId = crypto.randomUUID();

    return`
        INSERT INTO avtale_innhold (id, avtale, deltaker_fornavn, deltaker_etternavn, deltaker_tlf, bedrift_navn, arbeidsgiver_fornavn, arbeidsgiver_etternavn, arbeidsgiver_tlf, veileder_fornavn, veileder_etternavn, veileder_tlf, oppfolging, tilrettelegging, start_dato, slutt_dato, stillingprosent, journalpost_id, godkjent_av_deltaker, godkjent_av_arbeidsgiver, godkjent_av_veileder, godkjent_pa_vegne_av, ikke_bank_id, reservert, digital_kompetanse, arbeidsgiver_kontonummer, stillingstittel, arbeidsoppgaver, lonnstilskudd_prosent, manedslonn, feriepengesats, arbeidsgiveravgift, versjon, mentor_fornavn, mentor_etternavn, mentor_oppgaver, mentor_antall_timer, mentor_timelonn, har_familietilknytning, familietilknytning_forklaring, feriepenger_belop, otp_belop, arbeidsgiveravgift_belop, sum_lonnsutgifter, sum_lonnstilskudd, stillingstype, stilling_styrk08, stilling_konsept_id, manedslonn100pst, otp_sats, godkjent_av_nav_ident, sum_lønnstilskudd_redusert, dato_for_redusert_prosent, antall_dager_per_uke, ikrafttredelsestidspunkt, avtale_inngått, godkjent_av_beslutter, godkjent_av_beslutter_nav_ident, innhold_type, godkjent_pa_vegne_av_arbeidsgiver, klarer_ikke_gi_fa_tilgang, vet_ikke_hvem_som_kan_gi_tilgang, far_ikke_tilgang_personvern, enhet_kostnadssted, enhetsnavn_kostnadssted, refusjon_kontaktperson_fornavn, refusjon_kontaktperson_etternavn, refusjon_kontaktperson_tlf, ønsker_varsling_om_refusjon, inkluderingstilskudd_begrunnelse, godkjent_taushetserklæring_av_mentor, mentor_tlf, arena_migrering_deltaker, arena_migrering_arbeidsgiver)
        VALUES ('${avtaleInhholdId}', null, '${generateFornavn()}', '${generateEtternavn()}', '${generateTlf()}', '${generateBedriftsnavn()}', '${generateFornavn()}', '${generateEtternavn()}', '${generateTlf()}', '${generateFornavn()}', '${generateEtternavn()}', '${generateTlf()}', 'Telefon hver uke', 'Ingen', '${formatDato(datoFra)}', '${formatDato(datoTil)}', 50, 1, '${datoFra}', '${datoFra}', '${datoFra}', false, null, null, null, null, 'Butikkbetjent', 'Butikkarbeid', null, null, null, null, 1, null, null, null, null, null, null, null, null, null, null, null, null, null, 5223, 112968, null, null, 'Q987654', null, null, 5, '${datoFra}', '${datoFra}', null, null, 'INNGÅ', false, null, null, null, null, null, null, null, null, true, null, null, null, null, null)
        ON CONFLICT DO NOTHING;

        INSERT INTO maal (id, opprettet_tidspunkt, kategori, beskrivelse, avtale_innhold) VALUES ('${crypto.randomUUID()}', '${datoFra}', 'ARBEIDSERFARING', 'Dette er et bra mål!', '${avtaleInhholdId}');

        INSERT INTO avtale (id, opprettet_tidspunkt, deltaker_fnr, bedrift_nr, arbeidsgiver_fnr, veileder_nav_ident, arbeidstrening_lengde, gammel_godkjent_av_deltaker, gammel_godkjent_av_arbeidsgiver, gammel_godkjent_av_veileder, avbrutt, tiltakstype, sist_endret, avbrutt_dato, avbrutt_grunn, enhet_oppfolging, enhet_geografisk, slettemerket, annullert_tidspunkt, annullert_grunn, feilregistrert, enhetsnavn_geografisk, enhetsnavn_oppfolging, kvalifiseringsgruppe, formidlingsgruppe, godkjent_for_etterregistrering, gjeldende_innhold_id, mentor_fnr, opphav, status)
        VALUES ('${avtaleId}', '${datoFra}', '${fnr}', '${bedriftNr}', null, 'Z123456', null, null, null, null, ${avbruttDato ? 'true' : 'false'}, 'ARBEIDSTRENING', '${datoFra}', ${avbruttDato ? `'${formatDato(avbruttDato)}'` : 'null'}, ${avbruttDato ? '\'IKKE MØTT\'' : 'null'}, null, null, false, ${annullertDato ? `'${annullertDato}'` : 'null'}, ${annullertDato ? '\'IKKE MØTT\'' : 'null'}, false, null, null, null, null, false, '${avtaleInhholdId}', null, 'VEILEDER', '${status}')
        ON CONFLICT DO NOTHING;

        UPDATE avtale_innhold SET avtale='${avtaleId}' WHERE id='${avtaleInhholdId}';
    `;
}

const genererArenaTiltak = (id, tiltakstatus, bedriftNr, datoFra, datoTil, eksternId) => `
    INSERT INTO arena_ords_arbeidsgiver (arbgiv_id_arrangor, virksomhetsnummer, organisasjonsnummer_morselskap)
    VALUES ('${id}', '${bedriftNr}', '123456789')
    ON CONFLICT DO NOTHING;

    INSERT INTO arena_tiltakgjennomforing(tiltakgjennomforing_id, sak_id, tiltakskode, antall_deltakere, antall_varighet, dato_fra, dato_til, fagplankode, maaleenhet_varighet, tekst_fagbeskrivelse, tekst_kurssted, tekst_maalgruppe, status_treverdikode_innsokning, reg_dato, reg_user, mod_dato, mod_user, lokaltnavn, tiltakstatuskode, prosent_deltid, kommentar, arbgiv_id_arrangor, profilelement_id_geografi, klokketid_fremmote, dato_fremmote, begrunnelse_status, avtale_id, aktivitet_id, dato_innsokningstart, gml_fra_dato, gml_til_dato, aetat_fremmotereg, aetat_konteringssted, opplaeringnivaakode, tiltakgjennomforing_id_rel, vurdering_gjennomforing, profilelement_id_oppl_tiltak, dato_oppfolging_ok, partisjon, maalform_kravbrev, ekstern_id)
    VALUES ('${id}', '${id}', 'ARBTREN', 1, null, '${formatDato(datoFra)}', '${formatDato(datoTil)}', null, null, null, null, null, null, '${formatDato(datoFra)}', 'ARBLINJE', '${formatDato(datoFra)}', 'BRUKER', null, '${tiltakstatus}', 50, null, '${id}', null, null, null, null, null, '133292332', null, null, null, '1219', '1219', null, null, null, null, null, null, 'NO', '${eksternId}')
    ON CONFLICT DO NOTHING;
`;

const genererArenaDeltaker = (id, deltakerstatus, fnr, datoFra, datoTil, eksternId) => {
    let deltaker = `
        INSERT INTO arena_ords_fnr (person_id, fnr)
        VALUES ('${id}', '${fnr}')
        ON CONFLICT DO NOTHING;
    
        INSERT INTO arena_tiltakdeltaker(tiltakdeltaker_id, person_id, tiltakgjennomforing_id, deltakerstatuskode, deltakertypekode, aarsakverdikode_status, oppmotetypekode, prioritet, begrunnelse_innsokt, begrunnelse_prioritering, reg_dato, reg_user, mod_dato, mod_user, dato_svarfrist, dato_fra, dato_til, begrunnelse_status, prosent_deltid, brukerid_statusendring, dato_statusendring, aktivitet_id, brukerid_endring_prioritering, dato_endring_prioritering, dokumentkode_siste_brev, status_innsok_pakke, status_opptak_pakke, opplysninger_innsok, partisjon, begrunnelse_bestilling, antall_dager_pr_uke, ekstern_id)
        VALUES ('${id}', '${id}', '${id}', '${deltakerstatus}', null, null, null, null, null, null, '${formatDato(datoFra)}', 'BRUKER', '${formatDato(datoFra)}', 'BRUKER', null, '${formatDato(datoFra)}', '${formatDato(datoTil)}', null, 50, 'BRUKER', '${formatDato(datoFra)}', 1, null, null, null, null, null, null, null, null, 5, '${eksternId}')
        ON CONFLICT DO NOTHING;
    `;

    if (Math.random() >= 0.75) {
        const nyId = id + 1000000;
        deltaker += `
            INSERT INTO arena_ords_fnr (person_id, fnr)
            VALUES ('${nyId}', '${generateRandomSSN()}')
            ON CONFLICT DO NOTHING;
        
            INSERT INTO arena_tiltakdeltaker(tiltakdeltaker_id, person_id, tiltakgjennomforing_id, deltakerstatuskode, deltakertypekode, aarsakverdikode_status, oppmotetypekode, prioritet, begrunnelse_innsokt, begrunnelse_prioritering, reg_dato, reg_user, mod_dato, mod_user, dato_svarfrist, dato_fra, dato_til, begrunnelse_status, prosent_deltid, brukerid_statusendring, dato_statusendring, aktivitet_id, brukerid_endring_prioritering, dato_endring_prioritering, dokumentkode_siste_brev, status_innsok_pakke, status_opptak_pakke, opplysninger_innsok, partisjon, begrunnelse_bestilling, antall_dager_pr_uke, ekstern_id)
            VALUES ('${nyId}', '${nyId}', '${id}', '${deltakerstatus}', null, null, null, null, null, null, '${formatDato(datoFra)}', 'BRUKER', '${formatDato(datoFra)}', 'BRUKER', null, '${formatDato(datoFra)}', '${formatDato(datoTil)}', null, 50, 'BRUKER', '${formatDato(datoFra)}', 1, null, null, null, null, null, null, null, null, 5, '${eksternId}')
            ON CONFLICT DO NOTHING;
        `;
    }

    return deltaker;
}

const printProgress = (progressPercentage) => {
    process.stdout.clearLine();
    process.stdout.cursorTo(0);
    process.stdout.write(`Progress: ${progressPercentage}%`);
}

const migration = [];

statuser.forEach((s, idx) => {
    const {status, tiltakstatus, deltakerstatus, tid, antall} = s;

    for (let i = 0; i < antall; i++) {
        const id = migration.length;
        const avtaleId = crypto.randomUUID();
        const fnr = generateRandomSSN();
        const bedriftNr = generateBedriftnr();

        if (status === 'ANNULLERT') {
            const avtaleFra = addRandomDaysFromNow(30, -365);
            const avtaleTil = addRandomDays(avtaleFra, 180);
            migration.push(
                generateAvtale(avtaleId, fnr, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), status, randomDayBetween(avtaleFra, avtaleTil).toISOString()),
            );
        } else if (status === 'AVBRUTT') {
            const avtaleFra = addRandomDaysFromNow(30, -365);
            const avtaleTil = addRandomDays(avtaleFra, 180);
            migration.push(
                generateAvtale(avtaleId, fnr, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), status, null, randomDayBetween(avtaleFra, avtaleTil).toISOString()),
            );
        } else if (status === 'AVSLUTTET') {
            const avtaleTil = addRandomDaysFromNow(-1, -365);
            const avtaleFra = addRandomDays(avtaleTil, -1, -180);
            migration.push(
                generateAvtale(avtaleId, fnr, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), status),
            );
        } else if (status === 'KLAR_FOR_OPPSTART') {
            const avtaleFra = addRandomDaysFromNow(10);
            const avtaleTil = addRandomDays(avtaleFra, 180);
            migration.push(
                generateAvtale(avtaleId, fnr, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), status),
            );
        } else {
            const avtaleTil = addRandomDaysFromNow(180);
            const avtaleFra = addRandomDaysFromNow(-1, -180);
            migration.push(
                generateAvtale(avtaleId, fnr, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), status),
            );
        }

        if (tid === 'FREMOVER') {
            const avtaleTil = addRandomDaysFromNow(180);
            const avtaleFra = addRandomDays(avtaleTil, -1, -180);
            migration.push(
                genererArenaTiltak(id, tiltakstatus, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), avtaleId)
            );

            if (deltakerstatus) {
                migration.push(
                    genererArenaDeltaker(id, deltakerstatus, fnr, avtaleFra.toISOString(), avtaleTil.toISOString(), avtaleId)
                );
            }
        } else {
            const avtaleTil = addRandomDaysFromNow(-1, -365);
            const avtaleFra = addRandomDays(avtaleTil, -1, -180);
            migration.push(
                genererArenaTiltak(id, tiltakstatus, bedriftNr, avtaleFra.toISOString(), avtaleTil.toISOString(), avtaleId)
            );

            if (deltakerstatus) {
                migration.push(
                    genererArenaDeltaker(id, deltakerstatus, fnr, avtaleFra.toISOString(), avtaleTil.toISOString(), avtaleId)
                );
            }
        }
    }

    printProgress(Math.floor(idx / statuser.length * 100));
});

printProgress(100);

try {
    fs.mkdir('./src/main/resources/db/callbacks/local', { recursive: true }, (err) => { if (err) throw err; });
    fs.writeFileSync('./src/main/resources/db/callbacks/local/afterMigrate__arena.sql', migration.join('').replaceAll(/[ \t]{2,}/g, ' '));
} catch (err) {
    console.error(err);
}
