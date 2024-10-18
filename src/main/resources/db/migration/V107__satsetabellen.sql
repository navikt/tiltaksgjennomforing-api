begin;

create table satser (
    id uuid primary key,
    sats_type varchar(255) not null,
    sats_verdi numeric not null,
    gyldig_fra_og_med date not null,
    gyldig_til_og_med date,
    opprettet_tidspunkt timestamp with time zone not null default now()
);

create index satser_satstype_idx on satser(sats_type);

commit;
