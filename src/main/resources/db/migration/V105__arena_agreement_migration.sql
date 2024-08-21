CREATE TABLE arena_agreement_migration
(
    tiltakgjennomforing_id   int        primary key,
    status                   varchar    not null,
    agreement_id             uuid,
    FOREIGN KEY (tiltakgjennomforing_id) REFERENCES arena_tiltakgjennomforing(tiltakgjennomforing_id) ON DELETE CASCADE,
    FOREIGN KEY (agreement_id) REFERENCES avtale(id) ON DELETE CASCADE
);

CREATE INDEX idx_arena_arena_agreement_migration_agreement_id ON arena_agreement_migration(agreement_id);
