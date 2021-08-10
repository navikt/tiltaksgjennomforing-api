package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.Value;

import java.util.UUID;

@Value
public class RefusjonVarselMelding {
    UUID avtaleId;
    UUID tilskuddsperiodeId;
    VarselType varselType;
}