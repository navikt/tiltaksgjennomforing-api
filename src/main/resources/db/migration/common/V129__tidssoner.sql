alter table arbeidsgiver_notifikasjon alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table arena_agreement_migration alter column created type timestamp with time zone
    using created at time zone 'Europe/Oslo';

alter table arena_agreement_migration alter column modified type timestamp with time zone
    using modified at time zone 'Europe/Oslo';

alter table arena_event alter column created type timestamp with time zone
    using created at time zone 'Europe/Oslo';

alter table avtale alter column opprettet_tidspunkt type timestamp with time zone
    using opprettet_tidspunkt at time zone 'Europe/Oslo';

alter table avtale alter column sist_endret type timestamp with time zone
    using sist_endret at time zone 'UTC';

alter table avtale alter column annullert_tidspunkt type timestamp with time zone
    using annullert_tidspunkt at time zone 'UTC';

alter table avtale_forkortet alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'UTC';

alter table avtale_innhold alter column godkjent_av_deltaker type timestamp with time zone
    using godkjent_av_deltaker at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_arbeidsgiver type timestamp with time zone
    using godkjent_av_arbeidsgiver at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_veileder type timestamp with time zone
    using godkjent_av_veileder at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_av_beslutter type timestamp with time zone
    using godkjent_av_beslutter at time zone 'Europe/Oslo';

alter table avtale_innhold alter column godkjent_taushetserklæring_av_mentor type timestamp with time zone
    using godkjent_taushetserklæring_av_mentor at time zone 'Europe/Oslo';

alter table avtale_innhold alter column avtale_inngått type timestamp with time zone
    using avtale_inngått at time zone 'Europe/Oslo';

alter table avtale_innhold alter column ikrafttredelsestidspunkt type timestamp with time zone
    using ikrafttredelsestidspunkt at time zone 'Europe/Oslo';

alter table avtale_melding alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table dvh_melding alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table filter_sok alter column sist_sokt_tidspunkt type timestamp with time zone
    using sist_sokt_tidspunkt at time zone 'Europe/Oslo';

alter table inkluderingstilskuddsutgift alter column tidspunkt_lagt_til type timestamp with time zone
    using tidspunkt_lagt_til at time zone 'Europe/Oslo';

alter table maal alter column opprettet_tidspunkt type timestamp with time zone
    using opprettet_tidspunkt at time zone 'Europe/Oslo';

alter table oppgave alter column opprettet_tidspunkt type timestamp with time zone
    using opprettet_tidspunkt at time zone 'Europe/Oslo';

alter table sms alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table sporingslogg alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table tilskudd_periode alter column godkjent_tidspunkt type timestamp with time zone
    using godkjent_tidspunkt at time zone 'Europe/Oslo';

alter table tilskudd_periode alter column avslått_tidspunkt type timestamp with time zone
    using avslått_tidspunkt at time zone 'Europe/Oslo';

alter table varsel alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';
