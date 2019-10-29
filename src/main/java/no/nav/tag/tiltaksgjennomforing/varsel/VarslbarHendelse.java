package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import no.nav.tag.tiltaksgjennomforing.varsel.events.VarslbarHendelseOppstaatt;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class VarslbarHendelse extends AbstractAggregateRoot<VarslbarHendelse> {
    @Id
    private UUID id;
    private LocalDateTime tidspunkt;
    private UUID avtaleId;
    private VarslbarHendelseType varslbarHendelseType;

    public static VarslbarHendelse nyHendelse(Avtale avtale, VarslbarHendelseType varslbarHendelseType) {
        return nyHendelse(avtale, varslbarHendelseType, new GamleVerdier());
    }

    public static VarslbarHendelse nyHendelse(Avtale avtale, VarslbarHendelseType varslbarHendelseType, GamleVerdier gamleVerdier) {
        VarslbarHendelse varslbarHendelse = new VarslbarHendelse();
        varslbarHendelse.id = UUID.randomUUID();
        varslbarHendelse.tidspunkt = LocalDateTime.now();
        varslbarHendelse.avtaleId = avtale.getId();
        varslbarHendelse.varslbarHendelseType = varslbarHendelseType;
        varslbarHendelse.registerEvent(new VarslbarHendelseOppstaatt(avtale, varslbarHendelse, gamleVerdier));
        return varslbarHendelse;
    }

    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (tidspunkt == null) {
            tidspunkt = LocalDateTime.now();
        }
    }

}
