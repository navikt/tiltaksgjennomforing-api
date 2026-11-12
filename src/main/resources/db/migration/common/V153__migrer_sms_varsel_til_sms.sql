INSERT INTO sms (sms_varsel_id, telefonnummer, identifikator, meldingstekst, avtale_id, tidspunkt, hendelse_type, avsender_applikasjon)
SELECT sv.id,
       sv.telefonnummer,
       sv.identifikator,
       sv.meldingstekst,
       bv.avtale_id,
       bv.tidspunkt AT TIME ZONE 'Europe/Oslo',
       bv.varslbar_hendelse_type,
       'tiltaksgjennomforing-api'
FROM ikke_i_bruk_sms_varsel sv
JOIN (SELECT varslbar_hendelse, avtale_id, varslbar_hendelse_type, min(tidspunkt) as tidspunkt
   FROM ikke_i_bruk_bjelle_varsel
   GROUP BY varslbar_hendelse, avtale_id, varslbar_hendelse_type
) bv ON sv.varslbar_hendelse = bv.varslbar_hendelse
WHERE sv.status = 'SENDT'
  AND exists (select 1 from avtale a where a.id = bv.avtale_id)
  AND NOT exists (select 1 from sms s where s.sms_varsel_id = sv.id);
