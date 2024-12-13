package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avslagsårsak;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Beslutter;
import no.nav.tag.tiltaksgjennomforing.avtale.Deltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreStillingsbeskrivelse;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettMentorAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.ArbeidsgiverNotifikasjonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.AVTALE_FORDELT;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.AVTALE_FORLENGET;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.DELT_MED_ARBEIDSGIVER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.DELT_MED_DELTAKER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.DELT_MED_MENTOR;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.ENDRET;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.FJERNET_ETTERREGISTRERING;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENT_AV_ARBEIDSGIVER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENT_AV_DELTAKER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENT_FOR_ETTERREGISTRERING;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.GODKJENT_PAA_VEGNE_AV;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.NY_VEILEDER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.OPPRETTET;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.OPPRETTET_AV_ARBEIDSGIVER;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.STILLINGSBESKRIVELSE_ENDRET;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.TILSKUDDSBEREGNING_ENDRET;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.TILSKUDDSPERIODE_AVSLATT;
import static no.nav.tag.tiltaksgjennomforing.avtale.HendelseType.TILSKUDDSPERIODE_GODKJENT;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.avtalerMedTilskuddsperioder;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(Miljø.TEST)
@DirtiesContext
class LagVarselFraAvtaleHendelserTest {
    @Autowired
    AvtaleRepository avtaleRepository;
    @Autowired
    AvtaleInnholdRepository avtaleInnholdRepository;
    @Autowired
    VarselRepository varselRepository;
    @Autowired
    ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;
    @Autowired
    DvhMeldingEntitetRepository dvhMeldingEntitetRepository;
    @Autowired
    AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;
    @Autowired
    VeilarboppfolgingService veilarboppfolgingService;
    @Autowired
    SmsRepository smsRepository;

    @BeforeEach
    void setUp() {
        smsRepository.deleteAll();
        varselRepository.deleteAll();
        arbeidsgiverNotifikasjonRepository.deleteAll();
        avtaleInnholdRepository.deleteAll();
        dvhMeldingEntitetRepository.deleteAll();
        avtaleMeldingEntitetRepository.deleteAll();
        avtaleRepository.deleteAll();
    }

    @Test
    void test_alt() {
        Avtale avtale = avtaleRepository.save(Avtale.opprett(new OpprettAvtale(new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent()));
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);

        assertHendelse(OPPRETTET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(OPPRETTET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(OPPRETTET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);

        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleLønnstilskuddFelter(), Avtalerolle.ARBEIDSGIVER, avtalerMedTilskuddsperioder);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(ENDRET, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.VEILEDER, true);
        assertHendelse(ENDRET, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER, false);
        assertHendelse(ENDRET, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.DELTAKER, true);

        avtale.togglegodkjennEtterregistrering(TestData.enNavIdent());
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_FOR_ETTERREGISTRERING, AvtaleHendelseUtførtAvRolle.BESLUTTER, Avtalerolle.VEILEDER, true);

        avtale.togglegodkjennEtterregistrering(TestData.enNavIdent());
        avtale = avtaleRepository.save(avtale);
        assertHendelse(FJERNET_ETTERREGISTRERING, AvtaleHendelseUtførtAvRolle.BESLUTTER, Avtalerolle.VEILEDER, true);

        avtale.delMedAvtalepart(Avtalerolle.DELTAKER);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_DELTAKER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(DELT_MED_DELTAKER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
        assertIngenHendelse(DELT_MED_DELTAKER, Avtalerolle.ARBEIDSGIVER);

        avtale.delMedAvtalepart(Avtalerolle.ARBEIDSGIVER);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(DELT_MED_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertIngenHendelse(DELT_MED_ARBEIDSGIVER, Avtalerolle.DELTAKER);

        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(Now.instant(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_AV_DELTAKER, AvtaleHendelseUtførtAvRolle.DELTAKER, Avtalerolle.VEILEDER, true);
        assertHendelse(GODKJENT_AV_DELTAKER, AvtaleHendelseUtførtAvRolle.DELTAKER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(GODKJENT_AV_DELTAKER, AvtaleHendelseUtførtAvRolle.DELTAKER, Avtalerolle.DELTAKER, false);

        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(Now.instant(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.VEILEDER, true);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER, false);
        assertHendelse(GODKJENT_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.DELTAKER, true);

        arbeidsgiver.opphevGodkjenninger(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.VEILEDER, true);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER, false);
        assertHendelse(GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.DELTAKER, true);

        arbeidsgiver.godkjennAvtale(Now.instant(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(GODKJENT_PAA_VEGNE_AV, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertIngenHendelse(GODKJENT_PAA_VEGNE_AV, Avtalerolle.ARBEIDSGIVER);
        assertIngenHendelse(GODKJENT_PAA_VEGNE_AV, Avtalerolle.DELTAKER);

        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.avslåTilskuddsperiode(avtale, EnumSet.of(Avslagsårsak.FEIL_I_REGELFORSTÅELSE), "Forklaring");
        avtale = avtaleRepository.save(avtale);
        assertHendelse(TILSKUDDSPERIODE_AVSLATT, AvtaleHendelseUtførtAvRolle.BESLUTTER, Avtalerolle.VEILEDER, true);
        assertIngenHendelse(TILSKUDDSPERIODE_AVSLATT, Avtalerolle.ARBEIDSGIVER);
        assertIngenHendelse(TILSKUDDSPERIODE_AVSLATT, Avtalerolle.DELTAKER);

        veileder.sendTilbakeTilBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, TestData.ENHET_OPPFØLGING.getVerdi());
        avtale = avtaleRepository.save(avtale);
        assertHendelse(TILSKUDDSPERIODE_GODKJENT, AvtaleHendelseUtførtAvRolle.BESLUTTER, Avtalerolle.VEILEDER, true);
        assertIngenHendelse(TILSKUDDSPERIODE_GODKJENT, Avtalerolle.ARBEIDSGIVER);
        assertIngenHendelse(TILSKUDDSPERIODE_GODKJENT, Avtalerolle.DELTAKER);

        veileder.endreStillingbeskrivelse(EndreStillingsbeskrivelse.builder().stillingstittel("Tittel").arbeidsoppgaver("Oppgaver").stillingprosent(100.0).stillingKonseptId(1).stillingStyrk08(1).antallDagerPerUke(5.0).build(), avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(STILLINGSBESKRIVELSE_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(STILLINGSBESKRIVELSE_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(STILLINGSBESKRIVELSE_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);

        Veileder nyVeileder = TestData.enVeileder(new NavIdent("I000000"));
        nyVeileder.overtaAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(NY_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(NY_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(NY_VEILEDER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
    }

    @Test
    void test_for_arbeidsgiver_oppretter() {
        Avtale avtale = avtaleRepository.save(Avtale.opprett(new OpprettAvtale(new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.ARBEIDSGIVER));

        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.VEILEDER, true);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER, false);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.DELTAKER, true);

        Veileder veileder = TestData.enVeileder(TestData.enNavIdent());
        veileder.overtaAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
    }

    @Test
    void test_for_arbeidsgiver_oppretter_mentor_avtale() {
        Avtale avtale = avtaleRepository.save(Avtale.opprett(new OpprettMentorAvtale(new Fnr("00000000000"),new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MENTOR, Avtalerolle.ARBEIDSGIVER), Avtaleopphav.ARBEIDSGIVER));

        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.VEILEDER, true);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.ARBEIDSGIVER, false);
        assertHendelse(OPPRETTET_AV_ARBEIDSGIVER, AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, Avtalerolle.DELTAKER, true);

        Veileder veileder = TestData.enVeileder(TestData.enNavIdent());
        veileder.overtaAvtale(avtale);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
        assertHendelse(AVTALE_FORDELT, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.MENTOR, true);
    }

    @Test
    void test_for_delt_med_mentor() {
        Avtale avtale = avtaleRepository.save(Avtale.opprett(new OpprettMentorAvtale(new Fnr("00000000000") , new Fnr("00000000000"), new BedriftNr("999999999"), Tiltakstype.MENTOR, Avtalerolle.VEILEDER), Avtaleopphav.VEILEDER, TestData.enNavIdent()));
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleMentorFelter(), Avtalerolle.VEILEDER, avtalerMedTilskuddsperioder);
        avtale = avtaleRepository.save(avtale);

        avtale.delMedAvtalepart(Avtalerolle.DELTAKER);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_DELTAKER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(DELT_MED_DELTAKER, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
        assertIngenHendelse(DELT_MED_DELTAKER, Avtalerolle.ARBEIDSGIVER);

        avtale.delMedAvtalepart(Avtalerolle.MENTOR);
        avtale = avtaleRepository.save(avtale);
        assertHendelse(DELT_MED_MENTOR, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(DELT_MED_MENTOR, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.MENTOR, true);
        assertIngenHendelse(DELT_MED_MENTOR, Avtalerolle.ARBEIDSGIVER);

    }

    @Test
    void forleng_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        avtale = avtaleRepository.save(avtale);
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.forlengAvtale(avtale.getGjeldendeInnhold().getSluttDato().plusMonths(1), avtale);
        avtaleRepository.save(avtale);

        assertHendelse(AVTALE_FORLENGET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(AVTALE_FORLENGET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(AVTALE_FORLENGET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
    }

    @Test
    void endre_tilskuddsberegning() {
        Avtale avtale = avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder());
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning(), avtale);
        avtaleRepository.save(avtale);

        assertHendelse(TILSKUDDSBEREGNING_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.VEILEDER, false);
        assertHendelse(TILSKUDDSBEREGNING_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.ARBEIDSGIVER, true);
        assertHendelse(TILSKUDDSBEREGNING_ENDRET, AvtaleHendelseUtførtAvRolle.VEILEDER, Avtalerolle.DELTAKER, true);
    }

    private void assertHendelse(HendelseType hendelseType, AvtaleHendelseUtførtAvRolle utførtAv, Avtalerolle mottaker, boolean bjelle) {
        assertThat(varselRepository.findAll())
                .filteredOn(varsel -> varsel.getMottaker() == mottaker && varsel.getUtførtAv() == utførtAv && varsel.getHendelseType() == hendelseType && varsel.isBjelle() == bjelle)
                .hasSize(1);
    }

    private void assertIngenHendelse(HendelseType hendelseType, Avtalerolle mottaker) {
        assertThat(varselRepository.findAll())
                .filteredOn(varsel -> varsel.getMottaker() == mottaker && varsel.getHendelseType() == hendelseType)
                .isEmpty();
    }
}
