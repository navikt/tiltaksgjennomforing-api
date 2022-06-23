CREATE TABLE inkluderingstilskuddsutgift
(
    id uuid primary key,
-- TODO? Bør mappes med references avtale_innhold(id); som i V78?
    avtale_innhold uuid,
    beløp integer,
    type varchar,
    tidspunkt_lagt_til timestamp without time zone not null default now()
);

alter table avtale_innhold add column inkluderingstilskudd_begrunnelse varchar;