create table status_journalforing
(
    id                uuid primary key,
    avtale            uuid references avtale (id),
    tidspunkt         timestamp without time zone not null default now(),
    status            varchar
);