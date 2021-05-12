package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DvhAvtalehendelseLytter {
    private final DvhMeldingEntitetRepository repository;
    private final DvhMeldingFilter dvhMeldingFilter;

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        Avtale avtale = event.getAvtale();
        if (!dvhMeldingFilter.skalTilDatavarehus(avtale)) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.INNGÅTT;
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, avtale.getId(), tidspunkt, avtale.statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlenget event) {
        if (!dvhMeldingFilter.skalTilDatavarehus(event.getAvtale())) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.FORLENGET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortet event) {
        if (!dvhMeldingFilter.skalTilDatavarehus(event.getAvtale())) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.FORKORTET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        if (!dvhMeldingFilter.skalTilDatavarehus(event.getAvtale())) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.ANNULLERT;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }

    @EventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        if (!dvhMeldingFilter.skalTilDatavarehus(event.getAvtale())) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = DvhHendelseType.ENDRET;
        var melding = AvroTiltakHendelseFabrikk.konstruer(event.getAvtale(), tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, event.getAvtale().getId(), tidspunkt, event.getAvtale().statusSomEnum(), melding);
        repository.save(entitet);
    }
}
