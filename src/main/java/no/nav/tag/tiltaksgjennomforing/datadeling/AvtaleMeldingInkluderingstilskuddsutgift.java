package no.nav.tag.tiltaksgjennomforing.datadeling;

import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;

import java.util.List;
import java.util.UUID;

public record AvtaleMeldingInkluderingstilskuddsutgift(
    UUID id,
    String navn,
    Integer beløp,
    String beskrivelse,
    InkluderingstilskuddsutgiftType type
) {
    public static List<AvtaleMeldingInkluderingstilskuddsutgift> map(List<Inkluderingstilskuddsutgift> utgifter) {
        return utgifter.stream()
            .map(utgift -> new AvtaleMeldingInkluderingstilskuddsutgift(
                utgift.getId(),
                utgift.getNavn(),
                utgift.getBeløp(),
                utgift.getBeskrivelse(),
                utgift.getType()
            ))
            .toList();
    }
}
