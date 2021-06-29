package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.Value;

@Value
public class RefusjonKlarVarselMelding {
    String refusjonVarselId;
    String avtaleId;
}
