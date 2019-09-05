package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.SmsVarsel;

@Value
public class SmsVarselResultatMottatt {
    private final SmsVarsel smsVarsel;
}
