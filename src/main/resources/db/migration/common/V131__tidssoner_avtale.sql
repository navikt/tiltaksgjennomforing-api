alter table avtale alter column opprettet_tidspunkt type timestamp with time zone
    using opprettet_tidspunkt at time zone 'Europe/Oslo';

alter table avtale alter column sist_endret type timestamp with time zone
    using sist_endret at time zone 'UTC';

alter table avtale alter column annullert_tidspunkt type timestamp with time zone
    using annullert_tidspunkt at time zone 'UTC';
