package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@EqualsAndHashCode
@Getter
public class Periode {
    private final LocalDate start;
    private final LocalDate slutt;

    public Periode(LocalDate start, LocalDate slutt) {
        if (start.isAfter(slutt)) {
            throw new IllegalArgumentException("Startdato må være før eller lik som sluttdato");
        }
        this.start = start;
        this.slutt = slutt;
    }

    public PeriodeOverlapp overlapper(Periode annenPeriode) {
        if (start.isEqual(annenPeriode.start) && slutt.isEqual(annenPeriode.slutt)) {
            return PeriodeOverlapp.HELT;
        } else if (start.isEqual(annenPeriode.start) && slutt.isAfter(annenPeriode.slutt)) {
            return PeriodeOverlapp.FORKORTET;
        } else if (start.isEqual(annenPeriode.start) && slutt.isBefore(annenPeriode.slutt)) {
            return PeriodeOverlapp.FORLENGET;
        } else if (start.isBefore(annenPeriode.slutt) || annenPeriode.start.isBefore(slutt)) {
            return PeriodeOverlapp.DELVIS;
        } else {
            return PeriodeOverlapp.INGEN;
        }
    }

}
