package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import java.util.UUID;
import lombok.Value;

@Value
public class RefusjonGodkjentMelding {
    UUID avtaleId;
    UUID tilskuddsperiodeId;
}
