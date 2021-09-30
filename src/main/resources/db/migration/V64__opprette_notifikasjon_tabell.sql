create table arbeidsgiver_notifikasjoner
(
    id                     uuid primary key,
    tidspunkt              timestamp without time zone not null default now(),
    avtale_id              uuid,
    hendelse_type          varchar,
    virksomhetsnummer      varchar,
    lenke                  varchar,
    serviceCode            integer,
    serviceEdition         integer,
    hendelse_utfort        boolean
);
