package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@FieldNameConstants
public record InkluderingstilskuddsutgiftDTO(
    UUID id,
    String navn,
    Integer beløp,
    String beskrivelse,
    InkluderingstilskuddsutgiftType type,
    LocalDate startdato,
    LocalDate sluttdato
) {

    public InkluderingstilskuddsutgiftDTO(Inkluderingstilskuddsutgift dbEntitet) {
        this(
            dbEntitet.getId(),
            dbEntitet.getNavn(),
            dbEntitet.getBeløp(),
            dbEntitet.getBeskrivelse(),
            dbEntitet.getType(),
            Optional.ofNullable(dbEntitet.getTilskuddperiode()).map(TilskuddPeriode::getStartDato).orElse(null),
            Optional.ofNullable(dbEntitet.getTilskuddperiode()).map(TilskuddPeriode::getSluttDato).orElse(null)
        );
    }

    public static List<InkluderingstilskuddsutgiftDTO> map(AvtaleInnhold dbEntity) {
        return map(dbEntity.getInkluderingstilskuddsutgift());
    }

    public static List<InkluderingstilskuddsutgiftDTO> map(List<Inkluderingstilskuddsutgift> utgifter) {
        return utgifter.stream().map(InkluderingstilskuddsutgiftDTO::new).toList();
    }
}
