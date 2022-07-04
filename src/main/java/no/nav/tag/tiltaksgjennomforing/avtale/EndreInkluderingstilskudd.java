package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class EndreInkluderingstilskudd {
    List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgift;

    public Integer inkluderingstilskuddTotalBeløp() {
        return inkluderingstilskuddsutgift.stream().map(inkluderingstilskuddsutgift -> inkluderingstilskuddsutgift.getBeløp())
                .collect(Collectors.toList()).stream()
                .reduce(0, Integer::sum);
    }
}
