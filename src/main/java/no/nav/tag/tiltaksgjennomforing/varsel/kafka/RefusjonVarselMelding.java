package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.Value;

@Value
public class RefusjonVarselMelding {
    String avtaleId;
    VarselType varselType;
}

enum VarselType {
    KLAR,
    REVARSEL

}
