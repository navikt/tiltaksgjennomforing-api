alter table avtale_innhold add column otp_sats decimal;
update avtale_innhold set otp_sats = 2.0 where otp_sats is NULL;