package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import org.springframework.stereotype.Component;

import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;

@Component
public class SmsVarselMeldingMapperImpl implements SmsVarselMeldingMapper {

    @Override
    public SmsVarselMelding tilMelding(SmsVarsel smsVarsel) {
        SmsVarselMelding smsVarselMelding = new SmsVarselMelding();
        smsVarselMelding.setSmsVarselId(smsVarsel.getId());
        smsVarselMelding.setIdentifikator(smsVarsel.getIdentifikator());
        smsVarselMelding.setMeldingstekst(smsVarsel.getMeldingstekst());
        smsVarselMelding.setTelefonnummer(smsVarsel.getTelefonnummer());
        
        return smsVarselMelding;
    }

}
