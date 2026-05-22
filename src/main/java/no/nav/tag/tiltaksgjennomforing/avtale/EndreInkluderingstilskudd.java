package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.NoArgsConstructor;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@Value
@NoArgsConstructor
public class EndreInkluderingstilskudd {
    List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();

    public boolean erTom() {
        return inkluderingstilskuddsutgift.isEmpty();
    }

    public boolean harMangler() {
        return inkluderingstilskuddsutgift.stream().anyMatch(i -> Utils.erNoenTomme(i.getBeløp(), i.getType()));
    }

    public boolean overskriderMaksbelop(Avtale avtale) {
        int belop = inkluderingstilskuddsutgift.stream()
            .map(Inkluderingstilskuddsutgift::getBeløp)
            .reduce(0, Integer::sum);

        return belop > InkluderingstilskuddStrategy.getInkluderingstilskuddSats(avtale.getGjeldendeInnhold().getSluttDato());
    }
}
