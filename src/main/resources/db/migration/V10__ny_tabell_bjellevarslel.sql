create table bjelle_varsel
(
    id                uuid primary key,
    varslbar_hendelse uuid references varslbar_hendelse (id),
    lest              boolean,
    identifikator     varchar(11),
    varslingstekst     varchar
);