BEGIN;

ALTER TABLE avtale ADD COLUMN opphav VARCHAR(255);

UPDATE avtale SET opphav = 'VEILEDER' WHERE opprettet_av_arbeidsgiver = false;
UPDATE avtale SET opphav = 'ARBEIDSGIVER' WHERE opprettet_av_arbeidsgiver = true;

ALTER TABLE avtale ALTER COLUMN opphav SET NOT NULL;

COMMIT;
