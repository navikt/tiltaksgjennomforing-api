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
        lagHendelse(avtale, DvhHendelseType.INNGÅTT);
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlenget event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORLENGET);
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortet event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET);
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ANNULLERT);
    }

    @EventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @EventListener
    public void stillingsbeskrivelseEndret(StillingsbeskrivelseEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @EventListener
    public void kontaktinformasjonEndret(KontaktinformasjonEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @EventListener
    public void oppfølgingOgTilretteleggingEndret(OppfølgingOgTilretteleggingEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @EventListener
    public void målEndret(MålEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    private void lagHendelse(Avtale avtale, DvhHendelseType endret) {
        if (!dvhMeldingFilter.skalTilDatavarehus(avtale)) {
            return;
        }
        LocalDateTime tidspunkt = LocalDateTime.now();
        UUID meldingId = UUID.randomUUID();
        DvhHendelseType hendelseType = endret;
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, tidspunkt, meldingId, hendelseType);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, avtale.getId(), tidspunkt, avtale.statusSomEnum(), melding);
        repository.save(entitet);
    }
}
