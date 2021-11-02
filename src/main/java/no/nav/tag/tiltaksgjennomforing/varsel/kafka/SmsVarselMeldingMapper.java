package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;

public class SmsVarselMeldingMapper {
    public SmsVarselMelding tilMelding(SmsVarsel smsVarsel) {
        return new SmsVarselMelding(smsVarsel.getId(), smsVarsel.getIdentifikator(), smsVarsel.getTelefonnummer(), smsVarsel.getMeldingstekst());
    }
}