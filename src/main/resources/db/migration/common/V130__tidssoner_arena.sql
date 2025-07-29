alter table arena_agreement_migration alter column created type timestamp with time zone
    using created at time zone 'Europe/Oslo';

alter table arena_agreement_migration alter column modified type timestamp with time zone
    using modified at time zone 'Europe/Oslo';

alter table arena_event alter column created type timestamp with time zone
    using created at time zone 'Europe/Oslo';
