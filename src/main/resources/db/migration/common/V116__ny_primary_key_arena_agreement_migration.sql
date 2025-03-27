ALTER TABLE arena_agreement_migration ADD COLUMN id UUID;
UPDATE arena_agreement_migration SET id = gen_random_uuid();
ALTER TABLE arena_agreement_migration ALTER COLUMN id SET NOT NULL;

ALTER TABLE arena_agreement_migration DROP CONSTRAINT IF EXISTS arena_agreement_migration_pkey; -- Postgres
ALTER TABLE arena_agreement_migration DROP CONSTRAINT IF EXISTS constraint_8b1; -- H2
ALTER TABLE arena_agreement_migration DROP CONSTRAINT IF EXISTS constraint_8b1c; -- H2

ALTER TABLE arena_agreement_migration ADD PRIMARY KEY (id);
