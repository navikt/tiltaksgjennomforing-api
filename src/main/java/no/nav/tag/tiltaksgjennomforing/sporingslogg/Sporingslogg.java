package no.nav.tag.tiltaksgjennomforing.sporingslogg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class Sporingslogg {
    @Id
    private UUID id;
    private LocalDateTime tidspunkt;
    private UUID avtaleId;
    @Enumerated(EnumType.STRING)
    private VarslbarHendelseType hendelseType;

    public static Sporingslogg nyHendelse(Avtale avtale, VarslbarHendelseType hendelseType) {
        Sporingslogg varslbarHendelse = new Sporingslogg();
        varslbarHendelse.id = UUID.randomUUID();
        varslbarHendelse.tidspunkt = Now.localDateTime();
        varslbarHendelse.avtaleId = avtale.getId();
        varslbarHendelse.hendelseType = hendelseType;
        return varslbarHendelse;
    }
}
