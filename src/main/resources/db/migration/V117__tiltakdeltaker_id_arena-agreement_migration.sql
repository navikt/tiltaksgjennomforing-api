ALTER TABLE arena_agreement_migration ADD COLUMN tiltakdeltaker_id int;

UPDATE arena_agreement_migration aam
SET tiltakdeltaker_id = subquery.tiltakdeltaker_id
FROM (
    SELECT tiltakgjennomforing_id, tiltakdeltaker_id
    FROM arena_tiltakdeltaker
) as subquery
WHERE aam.tiltakgjennomforing_id = subquery.tiltakgjennomforing_id;

ALTER TABLE arena_agreement_migration
ADD CONSTRAINT arena_agreement_migration_tiltakdeltaker_id_fkey
FOREIGN KEY (tiltakdeltaker_id)
REFERENCES arena_tiltakdeltaker(tiltakdeltaker_id);
