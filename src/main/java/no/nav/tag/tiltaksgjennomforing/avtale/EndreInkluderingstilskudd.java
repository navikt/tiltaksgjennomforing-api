package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Getter;
import lombok.Setter;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class EndreInkluderingstilskudd {
    List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift = new ArrayList<>();

    public boolean erTom() {
        return inkluderingstilskuddsutgift.isEmpty();
    }

    public boolean harMangler() {
        return inkluderingstilskuddsutgift.stream().anyMatch(i -> Utils.erNoenTomme(i.getBeløp(), i.getType()));
    }

    public boolean harAlleTidligereUtgifter(List<Inkluderingstilskuddsutgift> utgifter) {
        Set<UUID> tidligereUtgifter = inkluderingstilskuddsutgift.stream()
            .map(Inkluderingstilskuddsutgift::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return tidligereUtgifter.equals(utgifter.stream().map(Inkluderingstilskuddsutgift::getId).collect(Collectors.toSet()));
    }

    public boolean overskriderMaksbelop(Avtale avtale) {
        int belop = inkluderingstilskuddsutgift.stream()
            .map(Inkluderingstilskuddsutgift::getBeløp)
            .reduce(0, Integer::sum);

        return belop > InkluderingstilskuddAvtaleInnholdStrategy.getInkluderingstilskuddSats(avtale.getGjeldendeInnhold().getSluttDato());
    }
}
