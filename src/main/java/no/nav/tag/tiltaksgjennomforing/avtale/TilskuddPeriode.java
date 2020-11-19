package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.ToString;
import lombok.Value;

import java.time.LocalDate;

@Value
@ToString
public class TilskuddPeriode {
    private Integer beløp;
    private LocalDate datoFom;
    private LocalDate datoTom;
    private boolean sistePeriode;
}
