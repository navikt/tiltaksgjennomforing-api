package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AvtaleInnholdStrategy {
    void endre(EndreAvtale endreAvtale);
    default void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {}
    default void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new RuntimeException("Ikke implementert");
    }
    Map<String, Object> alleFelterSomMåFyllesUt();
}
