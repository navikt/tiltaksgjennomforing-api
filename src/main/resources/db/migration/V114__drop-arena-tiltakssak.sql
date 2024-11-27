ALTER TABLE arena_tiltakgjennomforing DROP CONSTRAINT IF EXISTS constraint_798; -- H2
ALTER TABLE arena_tiltakgjennomforing DROP CONSTRAINT IF EXISTS arena_tiltakgjennomforing_sak_id_fkey; -- Postgres
DROP TABLE arena_tiltakssak;
