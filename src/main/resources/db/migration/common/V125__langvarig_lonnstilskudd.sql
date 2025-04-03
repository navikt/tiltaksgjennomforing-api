create table langvarig_lonnstilskudd
(
    id     uuid primary key,
    avtale uuid references avtale (id)
);
