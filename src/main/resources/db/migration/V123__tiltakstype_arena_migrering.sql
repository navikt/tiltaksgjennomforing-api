ALTER TABLE arena_agreement_migration ADD COLUMN tiltakstype VARCHAR;
UPDATE arena_agreement_migration SET tiltakstype = 'ARBEIDSTRENING';

ALTER TABLE arena_agreement_migration ALTER COLUMN tiltakstype SET NOT NULL;
