package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public sealed interface GenererTilskuddsperioder {
    static GenererTilskuddsperioder fra(LocalDate start, LocalDate slutt) {
        return new Dato(Periode.av(start, slutt));
    }

    static GenererTilskuddsperioder fra(List<Inkluderingstilskuddsutgift> utgifter) {
        return new Utgift(utgifter);
    }

    static Dato konverterTilDatoer(GenererTilskuddsperioder gt) {
        return Optional.ofNullable(gt)
            .filter(p -> p instanceof Dato)
            .map(p -> (Dato) p)
            .orElseThrow(() -> new IllegalArgumentException("Kan ikke konvertere til datoer"));
    }

    static Utgift konverterTilUtgifter(GenererTilskuddsperioder gt) {
        return Optional.ofNullable(gt)
            .filter(p -> p instanceof Utgift)
            .map(p -> (Utgift) p)
            .orElseThrow(() -> new IllegalArgumentException("Kan ikke konvertere til utgifter"));
    }

    record Dato(Periode periode) implements GenererTilskuddsperioder {}

    record Utgift(List<Inkluderingstilskuddsutgift> utgifter) implements GenererTilskuddsperioder {}
}
