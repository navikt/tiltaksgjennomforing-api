package no.nav.tag.tiltaksgjennomforing.hendelselogg;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Hendelselogg {
    @Id
    private UUID id;
    private UUID avtaleId;
    private LocalDateTime tidspunkt;
    @Enumerated(EnumType.STRING)
    private Avtalerolle utførtAv;
    @Enumerated(EnumType.STRING)
    private VarslbarHendelseType hendelse;

    public static Hendelselogg nyHendelse(UUID avtaleId, Avtalerolle utførtAv, VarslbarHendelseType hendelse) {
        Hendelselogg hendelselogg = new Hendelselogg();
        hendelselogg.setId(UUID.randomUUID());
        hendelselogg.setAvtaleId(avtaleId);
        hendelselogg.setTidspunkt(LocalDateTime.now());
        hendelselogg.setUtførtAv(utførtAv);
        hendelselogg.setHendelse(hendelse);
        return hendelselogg;
    }
}
