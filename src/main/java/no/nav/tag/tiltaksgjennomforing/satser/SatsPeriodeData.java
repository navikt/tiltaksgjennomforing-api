package no.nav.tag.tiltaksgjennomforing.satser;

import java.time.LocalDate;

public record SatsPeriodeData(
        Integer satsVerdi,
        LocalDate gyldigFraOgMed,
        LocalDate gyldigTilOgMed
) {
}
