package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.Value;

import java.util.UUID;

@Value
public class RefusjonUtbetaltMelding {
    UUID avtaleId;
    UUID tilskuddsperiodeId;
}
