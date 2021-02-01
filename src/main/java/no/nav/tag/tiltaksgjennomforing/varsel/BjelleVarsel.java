package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.IdentifikatorConverter;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
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
public class BjelleVarsel extends AbstractAggregateRoot<BjelleVarsel> {
    @Id
    private UUID id;
    private boolean lest;
    @Convert(converter = IdentifikatorConverter.class)
    private Identifikator identifikator;
    private String varslingstekst;
    @Enumerated(EnumType.STRING)
    private VarslbarHendelseType varslbarHendelseType;
    private UUID varslbarHendelse;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;

    public static String getVarslbarHendelseTekst(VarslbarHendelse varslbarHendelse, Avtale avtale) {
        if (varslbarHendelse.getVarslbarHendelseType() == VarslbarHendelseType.TILSKUDDSPERIODE_AVSLATT) {
            TilskuddPeriode gjeldendePeriode = avtale.gjeldendeTilskuddsperiode();
            String avslagÅrsaker = gjeldendePeriode.getAvslagsårsaker().stream()
                    .map(type ->  type.getTekst().toLowerCase()).collect(Collectors.joining(", "));
            return varslbarHendelse.getVarslbarHendelseType().getTekst()
                    .concat(gjeldendePeriode.getAvslåttAvNavIdent().asString())
                    .concat(". Årsak til retur: ")
                    .concat(avslagÅrsaker)
                    .concat(" Forklaring: ")
                    .concat(gjeldendePeriode.getAvslagsforklaring());

        }

        return varslbarHendelse.getVarslbarHendelseType().getTekst();
    }

    public static BjelleVarsel nyttVarsel(Identifikator identifikator, VarslbarHendelse varslbarHendelse, Avtale avtale) {
        BjelleVarsel varsel = new BjelleVarsel();
        varsel.id = UUID.randomUUID();
        varsel.tidspunkt = LocalDateTime.now();
        varsel.identifikator = identifikator;
        varsel.varslbarHendelse = varslbarHendelse.getId();
        varsel.varslingstekst = getVarslbarHendelseTekst(varslbarHendelse, avtale);
        varsel.varslbarHendelseType = varslbarHendelse.getVarslbarHendelseType();
        varsel.avtaleId = varslbarHendelse.getAvtaleId();
        return varsel;
    }

    public void settTilLest() {
        lest = true;
    }
}
