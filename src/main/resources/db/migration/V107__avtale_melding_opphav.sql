BEGIN;

ALTER TABLE avtale_melding ADD COLUMN opphav VARCHAR(255);

UPDATE avtale_melding as am
SET opphav = a.opphav
from avtale a where am.avtale_id = a.id;

ALTER TABLE avtale_melding ALTER COLUMN opphav SET NOT NULL;

COMMIT;
