package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DvhAvtalehendelseLytter {
    private final DvhMeldingEntitetRepository repository;

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.INNGÅTT;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.INNGÅTT;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlenget event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.FORLENGET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortet event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.FORKORTET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.ANNULLERT;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.ENDRET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }
}
