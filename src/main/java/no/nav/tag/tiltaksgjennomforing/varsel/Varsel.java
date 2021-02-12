package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    private String tekst;
    @Enumerated(EnumType.STRING)
    private VarslbarHendelseType hendelseType;
    private boolean bjelle;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;
    @Enumerated(EnumType.STRING)
    private Avtalerolle mottaker;
    @Enumerated(EnumType.STRING)
    private Avtalerolle utførtAv;

    private static String tilskuddsperiodeAvslåttTekst(Avtale avtale, String varslbarHendelseTekst) {
        TilskuddPeriode gjeldendePeriode = avtale.gjeldendeTilskuddsperiode();
        String avslagÅrsaker = gjeldendePeriode.getAvslagsårsaker().stream()
                .map(type -> type.getTekst().toLowerCase()).collect(Collectors.joining(", "));
        return varslbarHendelseTekst
                .concat(gjeldendePeriode.getAvslåttAvNavIdent().asString())
                .concat(". Årsak til retur: ")
                .concat(avslagÅrsaker)
                .concat(". Forklaring: ")
                .concat(gjeldendePeriode.getAvslagsforklaring());
    }

    private static String getVarslbarHendelseTekst(Avtale avtale, VarslbarHendelseType hendelseType) {
        if (hendelseType == VarslbarHendelseType.TILSKUDDSPERIODE_AVSLATT) {
            return tilskuddsperiodeAvslåttTekst(avtale, hendelseType.getTekst());
        }
        return hendelseType.getTekst();
    }

    public static Varsel nyttVarsel(Identifikator identifikator, boolean bjelle, Avtale avtale, Avtalerolle mottaker, Avtalerolle utførtAv, VarslbarHendelseType varslbarHendelseType, UUID avtaleId) {
        Varsel varsel = new Varsel();
        varsel.id = UUID.randomUUID();
        varsel.tidspunkt = LocalDateTime.now();
        varsel.identifikator = identifikator;
        varsel.tekst = getVarslbarHendelseTekst(avtale, varslbarHendelseType);
        varsel.hendelseType = varslbarHendelseType;
        varsel.avtaleId = avtaleId;
        varsel.bjelle = bjelle;
        varsel.mottaker = mottaker;
        varsel.utførtAv = utførtAv;
        return varsel;
    }

    public void settTilLest() {
        lest = true;
    }
}
