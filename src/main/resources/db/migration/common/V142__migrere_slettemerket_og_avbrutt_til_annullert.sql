UPDATE avtale SET feilregistrert = true WHERE id IN (SELECT id FROM avtale WHERE slettemerket = true);
UPDATE avtale SET annullert_grunn = avbrutt_grunn WHERE id IN (SELECT id FROM avtale WHERE status = 'AVBRUTT' AND avbrutt_grunn IS NOT NULL) AND annullert_grunn IS NULL;
UPDATE avtale SET annullert_tidspunkt = avbrutt_dato::timestamp WHERE id IN (SELECT id FROM avtale WHERE status = 'AVBRUTT' AND avbrutt_dato IS NOT NULL) AND annullert_tidspunkt IS NULL;
UPDATE avtale SET status = 'ANNULLERT' WHERE id IN (SELECT id FROM avtale WHERE avbrutt = true OR slettemerket = true OR status = 'AVBRUTT');
