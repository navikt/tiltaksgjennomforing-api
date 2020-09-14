package no.nav.tag.tiltaksgjennomforing.hendelselogg;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({ "dev" })
@DirtiesContext
class LyttPåHendelseTilHendelseloggTest {
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Autowired
    AvtaleRepository avtaleRepository;
    @Autowired
    HendelseloggRepository hendelseloggRepository;

    @Test
    void skal_logge_opprett_kall() {
        Avtale avtale = harOpprettetAvtale();
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.OPPRETTET, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_opprett_av_arbeidsgiver_kall() {
        Avtale avtale = harOpprettetAvtaleAvAg();
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    void skal_logge_endre_kall() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.ENDRET, Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    void skal_logge_godkjent_av_deltaker() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvDeltaker(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENT_AV_DELTAKER, Avtalerolle.DELTAKER);
    }

    @Test
    void skal_logge_godkjent_av_arbeidsgiver() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    void skal_logge_godkjent_av_veileder() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvDeltaker(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        ogGodkjentAvVeileder(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENT_AV_VEILEDER, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_godkjennn_på_vegne_av() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        ogGodkjentPåVegneAv(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_godkjennninger_opphevet_av_arbeidsgiver() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        ogOpphevetAvArbeidsgiver(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    void skal_logge_godkjennninger_opphevet_av_veileder() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        ogOpphevetAvVeileder(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_avbryt() {
        Avtale avtale = harOpprettetAvtale();
        ogAvbrutt(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.AVBRUTT, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_gjenopprett() {
        Avtale avtale = harOpprettetAvtale();
        ogAvbrutt(avtale);
        ogGjenopprettet(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.GJENOPPRETTET, Avtalerolle.VEILEDER);
    }

    @Test
    void skal_logge_låst_opp() {
        Avtale avtale = harOpprettetAvtale();
        ogEndretAvtale(avtale);
        ogGodkjentAvDeltaker(avtale);
        ogGodkjentAvArbeidsgiver(avtale);
        ogGodkjentAvVeileder(avtale);
        ogLåstOpp(avtale);
        sjekkAtHendelseErLogget(avtale, VarslbarHendelseType.LÅST_OPP, Avtalerolle.VEILEDER);
    }

    private Avtale harOpprettetAvtale() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent());
        avtaleRepository.save(avtale);
        return avtale;
    }

    private Avtale harOpprettetAvtaleAvAg() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtaleRepository.save(avtale);
        return avtale;
    }

    private void ogEndretAvtale(Avtale avtale) {
        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER);
        avtaleRepository.save(avtale);
    }

    private void ogGodkjentAvDeltaker(Avtale avtale) {
        new Deltaker(avtale.getDeltakerFnr(), avtale).godkjennForAvtalepart();
        avtaleRepository.save(avtale);
    }

    private void ogGodkjentAvArbeidsgiver(Avtale avtale) {
        new Arbeidsgiver(TestData.etFodselsnummer(), avtale).godkjennForAvtalepart();
        avtaleRepository.save(avtale);
    }

    private void ogGodkjentAvVeileder(Avtale avtale) {
        new Veileder(TestData.enNavIdent(), avtale).godkjennForAvtalepart();
        avtaleRepository.save(avtale);
    }

    private void ogGodkjentPåVegneAv(Avtale avtale) {
        new Veileder(TestData.enNavIdent(), avtale).godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn());
        avtaleRepository.save(avtale);
    }

    private void ogOpphevetAvArbeidsgiver(Avtale avtale) {
        new Arbeidsgiver(TestData.etFodselsnummer(), avtale).opphevGodkjenninger();
        avtaleRepository.save(avtale);
    }

    private void ogOpphevetAvVeileder(Avtale avtale) {
        new Veileder(TestData.enNavIdent(), avtale).opphevGodkjenninger();
        avtaleRepository.save(avtale);
    }

    private void ogLåstOpp(Avtale avtale) {
        new Veileder(TestData.enNavIdent(), avtale).låsOppAvtale();
        avtaleRepository.save(avtale);
    }

    private void ogAvbrutt(Avtale avtale) {
        AvbruttInfo avbruttInfo = new AvbruttInfo();
        avbruttInfo.setAvbruttDato(LocalDate.now());
        avbruttInfo.setAvbruttGrunn("En grunn");
        new Veileder(TestData.enNavIdent(), avtale).avbrytAvtale(Instant.now(), avbruttInfo);
        avtaleRepository.save(avtale);
    }

    private void ogGjenopprettet(Avtale avtale) {
        new Veileder(TestData.enNavIdent(), avtale).gjenopprettAvtale();
        avtaleRepository.save(avtale);
    }

    private void sjekkAtHendelseErLogget(Avtale avtale, VarslbarHendelseType godkjentAvDeltaker, Avtalerolle deltaker) {
        List<Hendelselogg> hendelser = hendelseloggRepository.findAllByAvtaleId(avtale.getId());
        assertThat(hendelser)
                .filteredOn(logg -> logg.getHendelse() == godkjentAvDeltaker)
                .filteredOn(logg -> logg.getUtførtAv() == deltaker)
                .hasSize(1);
    }
}