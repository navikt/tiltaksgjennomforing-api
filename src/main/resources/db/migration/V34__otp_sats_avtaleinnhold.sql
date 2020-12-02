alter table avtale_innhold add column otp_sats decimal;

UPDATE avtale_innhold
SET avtale_innhold.otp_sats = 0.02
WHERE avtale_innhold.avtale
IN (SELECT id from avtale WHERE avtale.tiltakstype = 'MIDLERTIDIG_LONNSTILSKUDD' OR tiltakstype = 'VARIG_LONNSTILSKUDD' )
AND avtale_innhold.otp_sats is NULL;