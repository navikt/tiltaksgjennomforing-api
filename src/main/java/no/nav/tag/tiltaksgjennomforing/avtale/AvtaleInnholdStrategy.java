package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.Map;

public interface AvtaleInnholdStrategy {
    void endre(EndreAvtale endreAvtale);
    default void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new RuntimeException("Ikke implementert");
    }
    Map<String, Object> alleFelterSomMåFyllesUt();
}
