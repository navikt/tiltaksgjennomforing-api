package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndretAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
    public void avtaleForlengetAvVeileder(AvtaleForlengetAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORLENGET, event.getUtførtAv());
    }

    @EventListener
    public void avtaleForkortetAvVeileder(AvtaleForkortetAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET, event.getUtførtAv(), event.getForkortetGrunn());
    }

    @EventListener
    public void avtaleForkortetAvArena(AvtaleForkortetAvArena event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.FORKORTET, Identifikator.ARENA);
    }

    @EventListener
    public void avtaleAnnullertAvVeileder(AnnullertAvVeileder event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ANNULLERT, event.getUtfortAv());
    }

    @EventListener
    public void avtaleAnnullertAvSystem(AnnullertAvSystem event) {
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
    public void stillingsbeskrivelseEndret(AvtaleEndretAvArena event) {
        lagHendelse(event.getAvtale(), DvhHendelseType.ENDRET, Identifikator.ARENA);
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
        lagHendelse(avtale, endret, utførtAv, null);
    }

    private void lagHendelse(Avtale avtale, DvhHendelseType hendelseType, Identifikator utførtAv, ForkortetGrunn forkortetGrunn) {
        if (!avtale.erAvtaleInngått()) {
            return;
        }
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, hendelseType, utførtAv.asString(), forkortetGrunn);
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(avtale, melding);
        repository.save(entitet);
    }
}
