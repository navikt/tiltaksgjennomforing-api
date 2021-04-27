create table dvh_melding
(
    melding_id uuid primary key,
    avtale_id  uuid references avtale (id),
    tidspunkt  timestamp,
    json       varchar
);