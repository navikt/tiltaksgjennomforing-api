package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;

public interface AvtaleInnholdStrategy {
    void endre(EndreAvtale endreAvtale);
    boolean erAltUtfylt();
    default void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {}
    default void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new RuntimeException("Ikke implementert");
    }
}
