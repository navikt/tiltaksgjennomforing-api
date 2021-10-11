create table arbeidsgiver_notifikasjon
(
    id                     uuid primary key,
    tidspunkt              timestamp without time zone not null default now(),
    avtale_id              uuid,
    hendelse_type          varchar,
    virksomhetsnummer      varchar,
    lenke                  varchar,
    service_code           integer,
    service_edition        integer,
    varsel_sendt_vellykket boolean,
    status_response        varchar,
    notifikasjon_lest      boolean
);
