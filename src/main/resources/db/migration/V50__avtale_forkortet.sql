create table avtale_forkortet_entitet(
    id uuid,
    avtale_id uuid references avtale(id),
    avtale_innhold_id uuid references avtale_innhold(id),
    tidspunkt timestamp,
    ny_slutt_dato date,
    grunn varchar,
    annet_grunn varchar
);