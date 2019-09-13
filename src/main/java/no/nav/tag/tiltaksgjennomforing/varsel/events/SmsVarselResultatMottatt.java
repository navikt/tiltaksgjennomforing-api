package no.nav.tag.tiltaksgjennomforing.varsel.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;

@Value
public class SmsVarselResultatMottatt {
    private final SmsVarsel smsVarsel;
}
