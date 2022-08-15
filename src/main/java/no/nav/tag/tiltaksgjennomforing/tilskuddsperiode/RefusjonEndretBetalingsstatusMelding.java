package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
public class RefusjonEndretBetalingsstatusMelding {

    String id;
    String refusjonId;
    String avtaleNr;
    Integer beløp;
    Integer løpenummer;
    String kontonummer;
    TilskuddsperiodeUtbetaltStatus status;
    LocalDate avregningsdato;
    String tilskuddsperiodeId;
}
