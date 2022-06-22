CREATE TABLE inkluderingstilskuddsutgift
(
    id uuid primary key,
    avtale_innhold uuid,
    beløp integer,
    type varchar,
    tidspunkt_lagt_til timestamp without time zone not null default now()
);