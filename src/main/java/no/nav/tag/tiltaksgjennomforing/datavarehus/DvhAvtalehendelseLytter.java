package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlenget;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DvhAvtalehendelseLytter {
    private final DvhMeldingEntitetRepository repository;

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        Avtale avtale = event.getAvtale();
        lagHendelse(avtale, DvhHendelseType.INNGÅTT, event.getUtførtAv());
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlenget event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORLENGET, event.getUtførtAv());
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET, event.getUtførtAv());
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvSystem event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET, event.getUtførtAv());
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ANNULLERT, event.getUtfortAv());
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvSystem event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ANNULLERT, event.getUtfortAv());
    }

    @EventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void stillingsbeskrivelseEndret(StillingsbeskrivelseEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void kontaktinformasjonEndret(KontaktinformasjonEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void oppfølgingOgTilretteleggingEndret(OppfølgingOgTilretteleggingEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void målEndret(MålEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void inkluderingstilskuddEndret(InkluderingstilskuddEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    @EventListener
    public void omMentorEndret(OmMentorEndret event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, event.getUtførtAv());
    }

    private void lagHendelse(Avtale avtale, DvhHendelseType endret, Identifikator utførtAv) {
        if(avtale.erAvtaleInngått()) {
            LocalDateTime tidspunkt = Now.localDateTime();
            UUID meldingId = UUID.randomUUID();
            DvhHendelseType hendelseType = endret;
            var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, tidspunkt, meldingId, hendelseType, utførtAv.asString());
            DvhMeldingEntitet entitet = new DvhMeldingEntitet(meldingId, avtale.getId(), tidspunkt, avtale.getStatus(), melding);
            repository.save(entitet);
        }
    }
}
