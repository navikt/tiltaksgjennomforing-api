CREATE COLLATION IF NOT EXISTS "nb-NO-x-icu" (provider = icu, locale = 'nb_NO.utf8');

ALTER TABLE avtale ALTER COLUMN annullert_grunn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN avbrutt_grunn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN enhetsnavn_geografisk SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN enhetsnavn_oppfolging SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN formidlingsgruppe SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN kvalifiseringsgruppe SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN opphav SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN status SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN tiltakstype SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale ALTER COLUMN veileder_nav_ident SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE avtale_forkortet ALTER COLUMN grunn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_forkortet ALTER COLUMN annet_grunn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_forkortet ALTER COLUMN utført_av SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE avtale_innhold ALTER COLUMN arbeidsgiver_etternavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN arbeidsgiver_fornavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN bedrift_navn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN deltaker_etternavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN deltaker_fornavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN mentor_etternavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN mentor_fornavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN veileder_etternavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_innhold ALTER COLUMN veileder_fornavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE avtale_melding ALTER COLUMN avtale_status SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE avtale_melding ALTER COLUMN hendelse_type SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE dvh_melding ALTER COLUMN tiltak_status SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE inkluderingstilskuddsutgift ALTER COLUMN type SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE maal ALTER COLUMN kategori SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE maal ALTER COLUMN beskrivelse SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE oppgave ALTER COLUMN tittel SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE oppgave ALTER COLUMN beskrivelse SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE oppgave ALTER COLUMN opplaering SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE sms ALTER COLUMN identifikator SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE sms ALTER COLUMN meldingstekst SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE sms ALTER COLUMN hendelse_type SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE sms ALTER COLUMN avsender_applikasjon SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE sporingslogg ALTER COLUMN hendelse_type SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE tilskudd_periode ALTER COLUMN refusjon_status SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE tilskudd_periode ALTER COLUMN status SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE tilskudd_periode ALTER COLUMN godkjent_av_nav_ident SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE tilskudd_periode ALTER COLUMN avslått_av_nav_ident SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE tilskudd_periode ALTER COLUMN avslagsforklaring SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE tilskudd_periode_avslagsårsaker ALTER COLUMN avslagsårsaker SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE varsel ALTER COLUMN identifikator SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE varsel ALTER COLUMN tekst SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE varsel ALTER COLUMN hendelse_type SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE varsel ALTER COLUMN utført_av SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE varsel ALTER COLUMN mottaker SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE varsel ALTER COLUMN utført_av_identifikator SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";

ALTER TABLE vtao ALTER COLUMN fadder_fornavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
ALTER TABLE vtao ALTER COLUMN fadder_etternavn SET DATA TYPE VARCHAR COLLATE "nb-NO-x-icu";
