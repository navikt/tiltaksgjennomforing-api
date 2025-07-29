alter table arbeidsgiver_notifikasjon alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';
