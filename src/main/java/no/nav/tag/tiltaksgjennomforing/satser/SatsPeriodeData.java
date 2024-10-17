package no.nav.tag.tiltaksgjennomforing.satser;

import java.time.LocalDate;

public record SatsPeriodeData(
        Double satsVerdi,
        LocalDate gyldigFra,
        LocalDate gyldigTil
) {
}
