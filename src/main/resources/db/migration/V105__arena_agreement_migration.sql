CREATE TABLE arena_agreement_migration
(
    tiltakgjennomforing_id   int        primary key,
    status                   varchar    not null,
    avtale_id                uuid,
    created                  timestamp  not null default now(),
    modified                 timestamp  not null,
    FOREIGN KEY (tiltakgjennomforing_id) REFERENCES arena_tiltakgjennomforing(tiltakgjennomforing_id) ON DELETE CASCADE,
    FOREIGN KEY (avtale_id) REFERENCES avtale(id) ON DELETE CASCADE
);
