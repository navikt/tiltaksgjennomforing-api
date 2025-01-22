package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.data.domain.AbstractAggregateRoot;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class Varsel extends AbstractAggregateRoot<Varsel> {
    @Id
    private UUID id;
    private boolean lest;
    @Convert(converter = IdentifikatorConverter.class)
    private Identifikator identifikator;
    @Convert(converter = IdentifikatorConverter.class)
    private Identifikator utførtAvIdentifikator;
    private String tekst;
    @Enumerated(EnumType.STRING)
    private HendelseType hendelseType;
    private boolean bjelle;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;
    @Enumerated(EnumType.STRING)
    private Avtalerolle mottaker;
    @Enumerated(EnumType.STRING)
    private AvtaleHendelseUtførtAvRolle utførtAv;

    private static String tilskuddsperiodeAvslåttTekst(TilskuddPeriode tilskuddPeriode, String hendelseTypeTekst) {
        String avslagÅrsaker = tilskuddPeriode.getAvslagsårsaker().stream()
                .map(type -> type.getTekst().toLowerCase()).collect(Collectors.joining(", "));
        return hendelseTypeTekst
                .concat(tilskuddPeriode.getAvslåttAvNavIdent().asString())
                .concat(". Årsak til retur: ")
                .concat(avslagÅrsaker)
                .concat(". Forklaring: ")
                .concat(tilskuddPeriode.getAvslagsforklaring());
    }

    private static String lagVarselTekst(Avtale avtale, TilskuddPeriode tilskuddPeriode, HendelseType hendelseType) {
        return switch (hendelseType) {
            case TILSKUDDSPERIODE_AVSLATT -> tilskuddsperiodeAvslåttTekst(tilskuddPeriode, hendelseType.getTekst());
            case TILSKUDDSPERIODE_GODKJENT -> {
                if (tilskuddPeriode != null && tilskuddPeriode.getStartDato() != null && tilskuddPeriode.getSluttDato() != null) {
                    DateTimeFormatter norskDatoformat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    yield hendelseType.getTekst() + "\n(" + tilskuddPeriode.getStartDato().format(norskDatoformat) + " til " + tilskuddPeriode.getSluttDato().format(norskDatoformat) + ")";
                } else {
                    yield hendelseType.getTekst();
                }
            }
            case AVTALE_FORKORTET, AVTALE_FORKORTET_AV_ARENA ->
                    "Avtale forkortet til " + avtale.getGjeldendeInnhold().getSluttDato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            case AVTALE_FORLENGET, AVTALE_FORLENGET_AV_ARENA ->
                    "Avtale forlenget til " + avtale.getGjeldendeInnhold().getSluttDato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            default -> hendelseType.getTekst();
        };
    }

    public static Varsel nyttVarsel(
            Identifikator identifikator,
            boolean bjelle,
            Avtale avtale,
            Avtalerolle mottaker,
            AvtaleHendelseUtførtAvRolle utførtAv,
            Identifikator utførtAvIdentifikator,
            HendelseType hendelseType,
            TilskuddPeriode tilskuddPeriode
    ) {
        Varsel varsel = new Varsel();
        varsel.id = UUID.randomUUID();
        varsel.tidspunkt = Now.localDateTime();
        varsel.identifikator = identifikator;
        varsel.utførtAvIdentifikator = utførtAvIdentifikator;
        varsel.tekst = lagVarselTekst(avtale, tilskuddPeriode, hendelseType);
        varsel.hendelseType = hendelseType;
        varsel.avtaleId = avtale.getId();
        varsel.bjelle = bjelle;
        varsel.mottaker = mottaker;
        varsel.utførtAv = utførtAv;
        return varsel;
    }

    public void settTilLest() {
        lest = true;
    }
}
