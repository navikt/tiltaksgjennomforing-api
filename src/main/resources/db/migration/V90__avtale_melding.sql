create table avtale_melding
(
    melding_id    uuid primary key,
    avtale_id     uuid references avtale (id),
    tidspunkt     timestamp,
    hendelse_type varchar,
    json          varchar
);
