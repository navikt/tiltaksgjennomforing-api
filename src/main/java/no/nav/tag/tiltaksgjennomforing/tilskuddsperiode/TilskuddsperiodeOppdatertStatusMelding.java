package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TilskuddsperiodeOppdatertStatusMelding {
    TilskuddPeriodeStatus status;
    UUID tilskuddsperiodeId;
    UUID avtaleId;
    String refusjonId;
}