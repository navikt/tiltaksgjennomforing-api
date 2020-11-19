package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

import java.time.LocalDate;

@Value
public class TilskuddPeriode {
    private Integer beløp;
    private LocalDate datoFom;
    private LocalDate datoTom;
}
