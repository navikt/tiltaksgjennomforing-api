package no.nav.tag.tiltaksgjennomforing.sporingslogg;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
public class Sporingslogg {
    @Id
    private UUID id;
    private Instant tidspunkt;
    private UUID avtaleId;
    @Enumerated(EnumType.STRING)
    private HendelseType hendelseType;

    public static Sporingslogg nyHendelse(Avtale avtale, HendelseType hendelseType) {
        Sporingslogg sporingslogg = new Sporingslogg();
        sporingslogg.id = UUID.randomUUID();
        sporingslogg.tidspunkt = Now.instant();
        sporingslogg.avtaleId = avtale.getId();
        sporingslogg.hendelseType = hendelseType;
        return sporingslogg;
    }
}
