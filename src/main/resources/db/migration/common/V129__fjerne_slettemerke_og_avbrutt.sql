UPDATE avtale SET feilregistrert = true WHERE id IN (SELECT id FROM avtale WHERE slettemerket = true);
UPDATE avtale SET status = 'ANNULLERT' WHERE id IN (SELECT id FROM avtale WHERE avbrutt = true OR slettemerket = true or status = 'AVBRUTT');
UPDATE avtale SET annullert_grunn = avbrutt_grunn WHERE id IN (SELECT id FROM avtale WHERE status = 'ANNULLERT' AND avbrutt_grunn IS NOT NULL) AND annullert_grunn IS NULL;
UPDATE avtale SET annullert_tidspunkt = avbrutt_dato::timestamp WHERE id IN (SELECT id FROM avtale WHERE status = 'ANNULLERT' AND avbrutt_dato IS NOT NULL) AND annullert_tidspunkt IS NULL;

ALTER TABLE avtale DROP COLUMN slettemerket;
ALTER TABLE avtale DROP COLUMN avbrutt;
ALTER TABLE avtale DROP COLUMN avbrutt_grunn;
ALTER TABLE avtale DROP COLUMN avbrutt_dato;
