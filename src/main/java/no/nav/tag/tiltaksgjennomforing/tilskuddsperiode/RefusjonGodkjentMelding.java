package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
public class RefusjonGodkjentMelding {
    UUID avtaleId;
    UUID tilskuddsperiodeId;
}
