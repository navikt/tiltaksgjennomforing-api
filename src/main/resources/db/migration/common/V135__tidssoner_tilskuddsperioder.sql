alter table tilskudd_periode alter column godkjent_tidspunkt type timestamp with time zone
    using godkjent_tidspunkt at time zone 'Europe/Oslo';

alter table tilskudd_periode alter column avslått_tidspunkt type timestamp with time zone
    using avslått_tidspunkt at time zone 'Europe/Oslo';
