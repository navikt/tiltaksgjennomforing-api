package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import java.util.Map;

public interface AvtaleInnholdStrategy {
    void endre(EndreAvtale endreAvtale);
    default void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new RuntimeException("Ikke implementert");
    }
    default void regnUtTotalLonnstilskudd() {
    }
    Map<String, Object> alleFelterSomMåFyllesUt();

    void endreSluttDato(LocalDate nySluttDato);
}
