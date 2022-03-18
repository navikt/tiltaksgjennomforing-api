package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class RefusjonVarselMelding {
    UUID avtaleId;
    UUID tilskuddsperiodeId;
    VarselType varselType;
    LocalDate fristForGodkjenning;
}