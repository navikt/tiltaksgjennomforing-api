package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;

import java.time.LocalDate;
import java.util.Map;

public interface AvtaleInnholdStrategy {
    void endre(EndreAvtale endreAvtale);
    default void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new RuntimeException("Ikke implementert");
    }

    Map<String, Object> alleFelterSomMåFyllesUt();

    void endreSluttDato(LocalDate nySluttDato);
}
