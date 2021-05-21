package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.hendelselogg.HendelseloggRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.oppgave.LagGosysVarselLytter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle.*;
import static no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(Miljø.LOCAL)
@DirtiesContext
class LagVarselFraAvtaleHendelserTest {
    @Autowired
    AvtaleRepository avtaleRepository;
    @Autowired
    VarselRepository varselRepository;
    @Autowired
    HendelseloggRepository hendelseloggRepository;
    @MockBean
    LagGosysVarselLytter lagGosysVarselLytter;

    @BeforeEach
    void setUp() {
        varselRepository.deleteAll();
        hendelseloggRepository.deleteAll();
        avtaleRepository.deleteAll();
    }

    @Test
    void test_alt() {
        Avtale avtale = avtaleRepository.save(Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent()));

        assertHendelse(OPPRETTET, VEILEDER, VEILEDER, false);
        assertHendelse(OPPRETTET, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(OPPRETTET, VEILEDER, DELTAKER, true);

        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), ARBEIDSGIVER, EnumSet.of(avtale.getTiltakstype()));
        avtale = avtaleRepository.save(avtale);
        assertHendelse(ENDRET, ARBEIDSGIVER, VEILEDER, true);
        assertHendelse(ENDRET, ARBEIDSGIVER, ARBEIDSGIVER, false);
        assertHendelse(ENDRET, ARBEIDSGIVER, DELTAKER, true);

        avtale.delMedAvtalepart(DELTAKER);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_DELTAKER, VEILEDER, VEILEDER, false);
        assertHendelse(DELT_MED_DELTAKER, VEILEDER, DELTAKER, true);
        assertIngenHendelse(DELT_MED_DELTAKER, ARBEIDSGIVER);

        avtale.delMedAvtalepart(ARBEIDSGIVER);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_ARBEIDSGIVER, VEILEDER, VEILEDER, false);
        assertHendelse(DELT_MED_ARBEIDSGIVER, VEILEDER, ARBEIDSGIVER, true);
        assertIngenHendelse(DELT_MED_ARBEIDSGIVER, DELTAKER);

        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(Instant.now(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_AV_DELTAKER, DELTAKER, VEILEDER, true);
        assertHendelse(GODKJENT_AV_DELTAKER, DELTAKER, ARBEIDSGIVER, true);
        assertHendelse(GODKJENT_AV_DELTAKER, DELTAKER, DELTAKER, false);

        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, VEILEDER, VEILEDER, false);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, VEILEDER, DELTAKER, true);

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, ARBEIDSGIVER, VEILEDER, true);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, ARBEIDSGIVER, ARBEIDSGIVER, false);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, ARBEIDSGIVER, DELTAKER, true);

        arbeidsgiver.opphevGodkjenninger(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, ARBEIDSGIVER, VEILEDER, true);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, ARBEIDSGIVER, ARBEIDSGIVER, false);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, ARBEIDSGIVER, DELTAKER, true);

        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_PAA_VEGNE_AV, VEILEDER, VEILEDER, false);
        assertIngenHendelse(GODKJENT_PAA_VEGNE_AV, ARBEIDSGIVER);
        assertIngenHendelse(GODKJENT_PAA_VEGNE_AV, DELTAKER);

        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, TestData.ENHET_OPPFØLGING);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(TILSKUDDSPERIODE_GODKJENT, BESLUTTER, VEILEDER, true);
        assertIngenHendelse(TILSKUDDSPERIODE_GODKJENT, ARBEIDSGIVER);
        assertIngenHendelse(TILSKUDDSPERIODE_GODKJENT, DELTAKER);

        veileder.låsOppAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(LÅST_OPP, VEILEDER, VEILEDER, false);
        assertHendelse(LÅST_OPP, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(LÅST_OPP, VEILEDER, DELTAKER, true);

        veileder.avbrytAvtale(Instant.now(), new AvbruttInfo(LocalDate.now(), "Annet"), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(AVBRUTT, VEILEDER, VEILEDER, false);
        assertHendelse(AVBRUTT, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(AVBRUTT, VEILEDER, DELTAKER, true);

        veileder.gjenopprettAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GJENOPPRETTET, VEILEDER, VEILEDER, false);
        assertHendelse(GJENOPPRETTET, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(GJENOPPRETTET, VEILEDER, DELTAKER, true);

        Veileder nyVeileder = TestData.enVeileder(new NavIdent("I000000"));
        nyVeileder.overtaAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(NY_VEILEDER, VEILEDER, VEILEDER, false);
        assertHendelse(NY_VEILEDER, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(NY_VEILEDER, VEILEDER, DELTAKER, true);

        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        deltaker.godkjennAvtale(Instant.now(), avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennAvtale(Instant.now(), avtale);
        beslutter.avslåTilskuddsperiode(avtale, EnumSet.of(Avslagsårsak.FEIL_I_REGELFORSTÅELSE), "Forklaring");
        avtale = avtaleRepository.save(avtale);
        assertHendelse(TILSKUDDSPERIODE_AVSLATT, BESLUTTER, VEILEDER, true);
        assertIngenHendelse(TILSKUDDSPERIODE_AVSLATT, ARBEIDSGIVER);
        assertIngenHendelse(TILSKUDDSPERIODE_AVSLATT, DELTAKER);
    }

    @Test
    void test_for_arbeidsgiver_oppretter() {
        Avtale avtale = avtaleRepository.save(Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD)));

        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, ARBEIDSGIVER, VEILEDER, true);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, ARBEIDSGIVER, ARBEIDSGIVER, false);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, ARBEIDSGIVER, DELTAKER, true);

        Veileder veileder = TestData.enVeileder(TestData.enNavIdent());
        veileder.overtaAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(AVTALE_FORDELT, VEILEDER, VEILEDER, false);
        assertHendelse(AVTALE_FORDELT, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(AVTALE_FORDELT, VEILEDER, DELTAKER, true);
    }

    @Test
    void forleng_avtale() {
        Avtale avtale = avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeileder());
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.forlengAvtale(avtale.getSluttDato().plusMonths(1), avtale);
        avtaleRepository.save(avtale);

        assertHendelse(AVTALE_FORLENGET, VEILEDER, VEILEDER, false);
        assertHendelse(AVTALE_FORLENGET, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(AVTALE_FORLENGET, VEILEDER, DELTAKER, true);
    }

    @Test
    void endre_tilskuddsberegning() {
        Avtale avtale = avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeileder());
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning(), avtale);
        avtaleRepository.save(avtale);

        assertHendelse(TILSKUDDSBEREGNING_ENDRET, VEILEDER, VEILEDER, false);
        assertHendelse(TILSKUDDSBEREGNING_ENDRET, VEILEDER, ARBEIDSGIVER, true);
        assertHendelse(TILSKUDDSBEREGNING_ENDRET, VEILEDER, DELTAKER, true);
    }

    private void assertHendelse(VarslbarHendelseType hendelseType, Avtalerolle utførtAv, Avtalerolle mottaker, boolean bjelle) {
        assertThat(varselRepository.findAll())
                .filteredOn(varsel -> varsel.getMottaker() == mottaker && varsel.getUtførtAv() == utførtAv && varsel.getHendelseType() == hendelseType && varsel.isBjelle() == bjelle)
                .hasSize(1);
    }

    private void assertIngenHendelse(VarslbarHendelseType hendelseType, Avtalerolle mottaker) {
        assertThat(varselRepository.findAll())
                .filteredOn(varsel -> varsel.getMottaker() == mottaker && varsel.getHendelseType() == hendelseType)
                .isEmpty();
    }
}