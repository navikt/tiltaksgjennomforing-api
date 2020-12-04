alter table avtale_innhold add column otp_sats decimal;

update avtale_innhold set avtale_innhold.otp_sats = 0.02 where avtale_innhold.avtale in (select id from avtale where avtale.tiltakstype = 'MIDLERTIDIG_LONNSTILSKUDD' OR tiltakstype = 'VARIG_LONNSTILSKUDD' ) and avtale_innhold.otp_sats is null;