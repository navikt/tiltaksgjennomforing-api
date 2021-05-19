package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DvhAvtalehendelseLytter {
    private final DvhMeldingEntitetRepository repository;
    private final DvhMeldingFilter dvhMeldingFilter;

    @TransactionalEventListener
    public void avtaleInngått(AvtaleInngått event) {
        Avtale avtale = event.getAvtale();
        lagHendelse(avtale, DvhHendelseType.INNGÅTT);
    }

    @TransactionalEventListener
    public void avtaleForlenget(AvtaleForlenget event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORLENGET);
    }

    @TransactionalEventListener
    public void avtaleForkortet(AvtaleForkortet event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET);
    }

    @TransactionalEventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ANNULLERT);
    }

    @TransactionalEventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @TransactionalEventListener
    public void stillingsbeskrivelseEndret(StillingsbeskrivelseEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @TransactionalEventListener
    public void kontaktinformasjonEndret(KontaktinformasjonEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET);
    }

    @TransactionalEventListener
    public void oppfølgingOgTilretteleggingEndret(OppfølgingOgTilretteleggingEndret event) {
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
