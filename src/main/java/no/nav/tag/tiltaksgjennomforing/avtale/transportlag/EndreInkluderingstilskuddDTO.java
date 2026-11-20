package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.Getter;
import lombok.Setter;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddAvtaleInnholdStrategy;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class EndreInkluderingstilskuddDTO {
    List<InkluderingstilskuddsutgiftDTO> inkluderingstilskuddsutgift = new ArrayList<>();

    public boolean erTom() {
        return inkluderingstilskuddsutgift.isEmpty();
    }

    public boolean harMangler(boolean toggle) {
        if (toggle) {
            return inkluderingstilskuddsutgift.stream().anyMatch(i -> Utils.erNoenTomme(i.beløp(), i.type(), i.startdato(), i.sluttdato()));
        }
        return inkluderingstilskuddsutgift.stream().anyMatch(i -> Utils.erNoenTomme(i.beløp(), i.type()));
    }

    public boolean harAlleTidligereUtgifter(List<Inkluderingstilskuddsutgift> utgifter) {
        Set<UUID> tidligereUtgifter = inkluderingstilskuddsutgift.stream()
            .map(InkluderingstilskuddsutgiftDTO::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return tidligereUtgifter.equals(utgifter.stream().map(Inkluderingstilskuddsutgift::getId).collect(Collectors.toSet()));
    }

    public boolean overskriderMaksbelop(Avtale avtale) {
        int belop = inkluderingstilskuddsutgift.stream()
            .map(InkluderingstilskuddsutgiftDTO::beløp)
            .reduce(0, Integer::sum);

        return belop > InkluderingstilskuddAvtaleInnholdStrategy.getInkluderingstilskuddSats(avtale.getGjeldendeInnhold().getSluttDato());
    }
}
