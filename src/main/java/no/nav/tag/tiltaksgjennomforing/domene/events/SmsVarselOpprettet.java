package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.SmsVarsel;

@Value
public class SmsVarselOpprettet {
    private final SmsVarsel smsVarsel;
}
