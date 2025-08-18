package no.nav.tag.tiltaksgjennomforing.datadeling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.ArbeidsgiversGodkjenningOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndretAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleNyVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.DeltakersGodkjenningOpphevetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.DeltakersGodkjenningOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltakerOgArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.SignertAvMentor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AvtaleHendelseLytter {

    private final AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;
    private final ObjectMapper objectMapper;

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        lagHendelse(event.getAvtale(), HendelseType.OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()));
    }

    @EventListener
    public void avtaleOpprettetAvArena(AvtaleOpprettetAvArena event) {
        lagHendelse(event.getAvtale(), HendelseType.OPPRETTET_AV_ARENA, AvtaleHendelseUtførtAv.system(Identifikator.ARENA));
    }

    @EventListener
    public void avtaleOpprettetAvVeileder(AvtaleOpprettetAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.OPPRETTET, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.ENDRET, AvtaleHendelseUtførtAv.fra(event.getAvtale(), event.getUtfortAv(), event.getUtfortAvRolle()));
    }

    @EventListener
    public void avtaleEndretAvArena(AvtaleEndretAvArena event) {
        lagHendelse(event.getAvtale(), HendelseType.ENDRET_AV_ARENA, AvtaleHendelseUtførtAv.system(Identifikator.ARENA));
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        lagHendelse(event.getAvtale(), HendelseType.AVTALE_INNGÅTT, AvtaleHendelseUtførtAv.fra(event.getAvtale(), event.getUtførtAv(), event.getUtførtAvRolle()));
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlengetAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.AVTALE_FORLENGET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void avtaleForlengetAvArena(AvtaleForlengetAvArena event) {
        lagHendelse(event.getAvtale(), HendelseType.AVTALE_FORLENGET_AV_ARENA, AvtaleHendelseUtførtAv.system(Identifikator.ARENA));
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.AVTALE_FORKORTET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), event.getForkortetGrunn());
    }

    @EventListener
    public void avtaleForkortetAvArena(AvtaleForkortetAvArena event) {
        lagHendelse(event.getAvtale(), HendelseType.AVTALE_FORKORTET_AV_ARENA, AvtaleHendelseUtførtAv.system(Identifikator.ARENA), event.getForkortetGrunn());
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.ANNULLERT, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvSystem event) {
        lagHendelse(event.getAvtale(), HendelseType.ANNULLERT, AvtaleHendelseUtførtAv.system(event.getUtfortAv()));
    }

    @EventListener
    public void tilskuddsberegningEndret(TilskuddsberegningEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.TILSKUDDSBEREGNING_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void stillingsbeskrivelseEndret(StillingsbeskrivelseEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.STILLINGSBESKRIVELSE_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void kontaktinformasjonEndret(KontaktinformasjonEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.KONTAKTINFORMASJON_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void oppfølgingOgTilretteleggingEndret(OppfølgingOgTilretteleggingEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void målEndret(MålEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.MÅL_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void inkluderingstilskuddEndret(InkluderingstilskuddEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.INKLUDERINGSTILSKUDD_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void omMentorEndret(OmMentorEndret event) {
        lagHendelse(event.getAvtale(), HendelseType.OM_MENTOR_ENDRET, AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()));
    }

    @EventListener
    public void nyVeilederPåAvtale(AvtaleNyVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.NY_VEILEDER, AvtaleHendelseUtførtAv.veileder(event.getAvtale().getVeilederNavIdent()));
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()));
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_AV_VEILEDER, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_AV_DELTAKER, AvtaleHendelseUtførtAv.deltaker(event.getUtfortAv()));
    }

    @EventListener
    public void godkjentPåVegneAvDeltaker(GodkjentPaVegneAvDeltaker event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_PAA_VEGNE_AV, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void godkjentPåVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void godkjentPåVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        lagHendelse(event.getAvtale(), HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER, AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()));
    }

    @EventListener
    public void signertAvMentor(SignertAvMentor event) {
        lagHendelse(event.getAvtale(), HendelseType.SIGNERT_AV_MENTOR, AvtaleHendelseUtførtAv.mentor(event.getUtfortAv()));
    }

    @EventListener
    public void deltakersGodkjenningOpphevetAvVeileder(DeltakersGodkjenningOpphevetAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.DELTAKERS_GODKJENNING_OPPHEVET_AV_VEILEDER, AvtaleHendelseUtførtAv.veileder(event.getAvtale().getVeilederNavIdent()));
    }

    @EventListener
    public void deltakersGodkjenningOpphevetAvArbeidsgiver(DeltakersGodkjenningOpphevetAvArbeidsgiver event) {
        lagHendelse(event.getAvtale(), HendelseType.DELTAKERS_GODKJENNING_OPPHEVET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()));
    }

    @EventListener
    public void arbeidsgiversGodkjenningOpphevetAvVeileder(ArbeidsgiversGodkjenningOpphevetAvVeileder event) {
        lagHendelse(event.getAvtale(), HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER, AvtaleHendelseUtførtAv.veileder(event.getAvtale().getVeilederNavIdent()));
    }

    private void lagHendelse(Avtale avtale, HendelseType hendelseType, AvtaleHendelseUtførtAv utførtAv) {
        lagHendelse(avtale, hendelseType, utførtAv, null);
    }

    private void lagHendelse(Avtale avtale, HendelseType hendelseType, AvtaleHendelseUtførtAv avtaleHendelseUtførtAv, ForkortetGrunn forkortetGrunn) {
        Instant tidspunkt = Now.instant();
        UUID meldingId = UUID.randomUUID();
        AvtaleMelding melding = AvtaleMelding.create(avtale, avtale.getGjeldendeInnhold(), avtaleHendelseUtførtAv, hendelseType, forkortetGrunn);
        try {
            String meldingSomString = objectMapper.writeValueAsString(melding);
            AvtaleMeldingEntitet entitet = new AvtaleMeldingEntitet(meldingId, avtale.getId(), tidspunkt, hendelseType, avtale.getStatus(), meldingSomString);
            avtaleMeldingEntitetRepository.save(entitet);
        } catch (JsonProcessingException e) {
            log.error("Feil ved parsing av AvtaleHendelseMelding til json for avtale med id: {}", avtale.getId());
            throw new RuntimeException(e);
        }
    }

}
