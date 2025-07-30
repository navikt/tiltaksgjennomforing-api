alter table avtale_melding alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';

alter table dvh_melding alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';
