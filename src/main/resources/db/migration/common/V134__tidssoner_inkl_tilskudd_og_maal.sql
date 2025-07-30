alter table inkluderingstilskuddsutgift alter column tidspunkt_lagt_til type timestamp with time zone
    using tidspunkt_lagt_til at time zone 'Europe/Oslo';

alter table maal alter column opprettet_tidspunkt type timestamp with time zone
    using opprettet_tidspunkt at time zone 'Europe/Oslo';
