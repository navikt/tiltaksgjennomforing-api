create table vtao
(
    id uuid primary key,
    fadder_fornavn varchar(255),
    fadder_etternavn varchar(255),
    fadder_tlf varchar(255),
    avtale_innhold_id uuid unique references avtale_innhold(id) not null
);