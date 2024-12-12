ALTER TABLE arena_agreement_migration ADD COLUMN tiltakdeltaker_id int;

UPDATE arena_agreement_migration aam
SET tiltakdeltaker_id = (
    SELECT tiltakdeltaker_id
    FROM arena_tiltakdeltaker
    WHERE tiltakgjennomforing_id = aam.tiltakgjennomforing_id
    LIMIT 1
);

ALTER TABLE arena_agreement_migration
ADD CONSTRAINT arena_agreement_migration_tiltakdeltaker_id_fkey
FOREIGN KEY (tiltakdeltaker_id)
REFERENCES arena_tiltakdeltaker(tiltakdeltaker_id);
