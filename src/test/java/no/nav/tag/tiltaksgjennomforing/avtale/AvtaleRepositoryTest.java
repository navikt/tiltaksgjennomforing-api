package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.datavarehus.DvhMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.metrikker.MetrikkRegistrering;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.ArbeidsgiverNotifikasjonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.ENHET_GEOGRAFISK;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.ENHET_OPPFØLGING;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.ARBEIDSTRENING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
@EmbeddedKafka
public class AvtaleRepositoryTest {

    @Autowired
    private AvtaleRepository avtaleRepository;

    @Autowired
    private VarselRepository varselRepository;

    @Autowired
    private SmsRepository smsRepository;

    @Autowired
    private AvtaleInnholdRepository avtaleInnholdRepository;

    @Autowired
    private DvhMeldingEntitetRepository dvhMeldingEntitetRepository;

    @Autowired
    private AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;

    @Autowired
    private ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @MockBean
    private MetrikkRegistrering metrikkRegistrering;


    @BeforeEach
    public void setup() {
        varselRepository.deleteAll();
        smsRepository.deleteAll();
        avtaleInnholdRepository.deleteAll();
        arbeidsgiverNotifikasjonRepository.deleteAll();
        dvhMeldingEntitetRepository.deleteAll();
        avtaleMeldingEntitetRepository.deleteAll();
        avtaleRepository.deleteAll();
    }

    @Test
    public void nyAvtaleSkalKunneLagreOgReturneresAvRepository() {
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        Optional<Avtale> avtaleOptional = avtaleRepository.findById(lagretAvtale.getId());
        assertThat(avtaleOptional).isPresent();
    }

    @Test
    public void skalKunneLagreMaalFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        Maal maal = TestData.etMaal();
        endreAvtale.setMaal(List.of(maal));
        lagretAvtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        Maal maal2 = TestData.etMaal();
        endreAvtale2.setMaal(List.of(maal2));
        lagretAvtale2.endreAvtale(endreAvtale2, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale2);
    }

    @Test
    public void skalKunneLagreOppgaverFlereGanger() {
        // Lage avtale
        Avtale lagretAvtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        lagretAvtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale);

        // Lage ny avtale
        Avtale lagretAvtale2 = avtaleRepository.save(TestData.enArbeidstreningAvtale());

        // Lagre maal skal enda fungere
        EndreAvtale endreAvtale2 = new EndreAvtale();
        lagretAvtale2.endreAvtale(endreAvtale2, Avtalerolle.VEILEDER);
        avtaleRepository.save(lagretAvtale2);
    }

    @Test
    public void skalKunneLagreTilskuddsPeriode() {
        // Lage avtale
        Avtale lagretAvtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        lagretAvtale.getGjeldendeInnhold().setSumLonnstilskudd(20000);
        lagretAvtale = avtaleRepository.save(lagretAvtale);

        // Lagre tilskuddsperiode skal fungere
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setStartDato(lagretAvtale.getGjeldendeInnhold().getStartDato());
        endreAvtale.setSluttDato(lagretAvtale.getGjeldendeInnhold().getSluttDato());
        endreAvtale.setManedslonn(20000);
        endreAvtale.setStillingprosent(BigDecimal.valueOf(100.0));
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setFeriepengesats(BigDecimal.valueOf(0.12));
        endreAvtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        endreAvtale.setLonnstilskuddProsent(40);

        lagretAvtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        Avtale nyLagretAvtale = avtaleRepository.save(lagretAvtale);

        var perioder = nyLagretAvtale.getTilskuddPeriode();
        assertThat(perioder).isNotEmpty();
        assertThat(lagretAvtale.getId()).isEqualTo(perioder.first().getAvtale().getId());
    }

    @Test
    public void avtale_godkjent_pa_vegne_av_skal_lagres_med_pa_vegne_av_grunn() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        godkjentPaVegneGrunn.setIkkeBankId(true);
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);

        assertThat(lagretAvtale.getGjeldendeInnhold().getGodkjentPaVegneGrunn().isIkkeBankId()).isEqualTo(godkjentPaVegneGrunn.isIkkeBankId());
    }

    @Test
    public void lagre_pa_vegne_skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);

        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentPaVegneAv(any());
    }

    @Test
    public void opprettAvtale__skal_publisere_domainevent() {
        Avtale nyAvtale = Avtale.opprett(new OpprettAvtale(Fnr.generer(1980, 2, 19), new BedriftNr("101033333"), ARBEIDSTRENING), Avtaleopphav.VEILEDER, new NavIdent("Q000111"));
        avtaleRepository.save(nyAvtale);
        verify(metrikkRegistrering).avtaleOpprettet(any());
    }

    @Test
    public void endreAvtale__skal_publisere_domainevent() {
        Avtale avtale = avtaleRepository.save(TestData.enArbeidstreningAvtale());
        verify(metrikkRegistrering, never()).avtaleEndret(any());
        avtale.endreAvtale(TestData.ingenEndring(), Avtalerolle.VEILEDER);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).avtaleEndret(any());
    }

    @Test
    public void godkjennForArbeidsgiver__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enArbeidsgiver(avtale).godkjennAvtale(avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvArbeidsgiver(any());
    }

    @Test
    public void godkjennForDeltaker__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enDeltaker(avtale).godkjennAvtale(avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvDeltaker(any());
    }

    @Test
    public void godkjennForVeileder__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        TestData.enVeileder(avtale).godkjennAvtale(avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjentAvVeileder(any());
    }

    @Test
    public void opphevGodkjenning__skal_publisere_domainevent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TestData.enArbeidsgiver(avtale).godkjennForAvtalepart(avtale);
        TestData.enVeileder(avtale).opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
        verify(metrikkRegistrering).godkjenningerOpphevet(any(GodkjenningerOpphevetAvVeileder.class));
    }

    @Test
    public void finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter__skal_ikke_kunne_hente_avtale_med_tiltakstype_arbeidstrening_3() {
        Avtale avtale = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(1), Now.localDate().plusMonths(3).plusDays(1));
        Avtale avtale2 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(5), Now.localDate().plusMonths(3).plusDays(5));
        Avtale avtale3 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(10), Now.localDate().plusMonths(3).plusDays(10));
        Avtale avtale4 = enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(Now.localDate().plusDays(15), Now.localDate().plusMonths(3).plusDays(15));
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Arne");
        avtale2.getGjeldendeInnhold().setDeltakerFornavn("Bjarne");
        avtale3.getGjeldendeInnhold().setDeltakerFornavn("Carl");

        avtaleRepository.save(avtale);
        avtaleRepository.save(avtale2);
        avtaleRepository.save(avtale3);
        avtaleRepository.save(avtale4);

        Set<String> navEnheter = Set.of(ENHET_OPPFØLGING.getVerdi());
        Set<Tiltakstype> tiltakstype = Set.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD);
        Sort by = Sort.by(Sort.Order.asc("startDato"));
        Pageable pageable = PageRequest.of(0, 10, by);
        long plussDato = ChronoUnit.DAYS.between(Now.localDate(), Now.localDate().plusMonths(3));

        Page<BeslutterOversiktEntity> beslutterOversikt = avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            TilskuddPeriodeStatus.UBEHANDLET,
            tiltakstype,
            Set.of(Status.PÅBEGYNT, Status.GJENNOMFØRES, Status.KLAR_FOR_OPPSTART, Status.MANGLER_GODKJENNING),
            navEnheter,
            null,
            null,
            false,
            pageable
        );

        assertThat(beslutterOversikt.getContent().size()).isEqualTo(4);
    }

    @Test
    public void findAllByDeltakerFnr__skal_kunne_hente_avtale_som_ikke_er_FEIL_REGISTRERT() {
        Pageable pageable = PageRequest.of(0, 100);
        Avtale lagretAvtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale.setFeilregistrert(false);

        Avtale lagretAvtaleFeilregistrert = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtaleFeilregistrert.setFeilregistrert(true);

        avtaleRepository.save(lagretAvtale);
        avtaleRepository.save(lagretAvtaleFeilregistrert);

        assertThat(lagretAvtale.getDeltakerFnr()).isEqualTo(lagretAvtaleFeilregistrert.getDeltakerFnr());

        Page<Avtale> avtalerFunnet = avtaleRepository
            .findAllByDeltakerFnrAndFeilregistrertIsFalse(lagretAvtale.getDeltakerFnr(), pageable);

        assertThat(avtalerFunnet.getContent()).isNotEmpty();
        assertThat(avtalerFunnet.getTotalElements()).isEqualTo(1);
        assertThat(avtalerFunnet.getContent().stream().findFirst().get().isFeilregistrert()).isFalse();
    }

    @Test
    public void findAllByAvtaleNr__skal_IKKE_kunne_hente_avtale_som_er_FEIL_REGISTRERT() {
        Pageable pageable = PageRequest.of(0, 100);
        Avtale lagretAvtaleFeilregistrert = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtaleFeilregistrert.setFeilregistrert(true);

        Avtale avtaleLagret = avtaleRepository.save(lagretAvtaleFeilregistrert);

        Page<Avtale> avtalerFunnet = avtaleRepository
            .findAllByAvtaleNrAndFeilregistrertIsFalse(avtaleLagret.getAvtaleNr(), pageable);

        assertThat(avtalerFunnet.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void findAllByBedriftNrAndTiltakstype_skal_kunne_hente_avtale_som_ikke_er_FEIL_REGISTRERT() {
        Pageable pageable = PageRequest.of(0, 100);
        Avtale lagretAvtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale.setFeilregistrert(true);
        lagretAvtale.setBedriftNr(new BedriftNr("123456789"));

        Avtale lagretAvtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale2.setFeilregistrert(false);
        lagretAvtale2.setBedriftNr(new BedriftNr("223456789"));

        Avtale lagretAvtale3 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale3.setFeilregistrert(true);
        lagretAvtale3.setBedriftNr(new BedriftNr("323456789"));

        avtaleRepository.save(lagretAvtale);
        avtaleRepository.save(lagretAvtale2);
        avtaleRepository.save(lagretAvtale3);

        Set<BedriftNr> bedriftNrSet = Set.of(lagretAvtale3.getBedriftNr(), lagretAvtale2.getBedriftNr(), lagretAvtale.getBedriftNr());

        Page<Avtale> avtalerFunnet = avtaleRepository
            .findAllByBedriftNrAndTiltakstype(
                bedriftNrSet,
                lagretAvtale.getTiltakstype(),
                pageable
            );

        assertThat(avtalerFunnet.getContent()).isNotEmpty();
        assertThat(avtalerFunnet.getTotalElements()).isEqualTo(1);
        assertThat(avtalerFunnet.getContent().stream().findFirst().get().isFeilregistrert()).isFalse();
    }

    @Test
    public void findAllByBedriftNrIn_skal_kunne_hente_avtale_som_ikke_er_FEIL_REGISTRERT() {
        Pageable pageable = PageRequest.of(0, 100);
        final LocalDate dato12UkerTilbakeFraNå = Now.localDate().minusWeeks(12);
        Avtale lagretAvtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale.setFeilregistrert(true);
        lagretAvtale.setBedriftNr(new BedriftNr("123456789"));

        Avtale lagretAvtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale2.setFeilregistrert(false);
        lagretAvtale2.setBedriftNr(new BedriftNr("223456789"));

        Avtale lagretAvtale3 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        lagretAvtale3.setFeilregistrert(true);
        lagretAvtale3.setBedriftNr(new BedriftNr("323456789"));

        avtaleRepository.save(lagretAvtale);
        avtaleRepository.save(lagretAvtale2);
        avtaleRepository.save(lagretAvtale3);

        Set<BedriftNr> bedriftNrSet = Set.of(lagretAvtale3.getBedriftNr(), lagretAvtale2.getBedriftNr(), lagretAvtale.getBedriftNr());

        Page<Avtale> avtalerFunnet = avtaleRepository
            .findAllByBedriftNr(bedriftNrSet, pageable);

        assertThat(avtalerFunnet.getContent()).isNotEmpty();
        assertThat(avtalerFunnet.getTotalElements()).isEqualTo(1);
        assertThat(avtalerFunnet.getContent().stream().findFirst().get().isFeilregistrert()).isFalse();
    }

    @Test
    public void findAllByMentorFnr_skal_kunne_hente_avtale_som_ikke_er_FEIL_REGISTRERT() {
        Pageable pageable = PageRequest.of(0, 100);
        final Fnr mentorFnr = Fnr.generer(1976, 12, 28);
        Avtale lagretAvtale = TestData.enMentorAvtaleSignert();
        lagretAvtale.setFeilregistrert(false);
        lagretAvtale.setMentorFnr(mentorFnr);

        Avtale lagretAvtale2 = TestData.enMentorAvtaleSignert();
        lagretAvtale2.setFeilregistrert(true);
        lagretAvtale2.setMentorFnr(mentorFnr);


        avtaleRepository.save(lagretAvtale);
        avtaleRepository.save(lagretAvtale2);


        Page<Avtale> avtalerFunnet = avtaleRepository.findAllByMentorFnr(mentorFnr, pageable);

        assertThat(avtalerFunnet.getContent()).isNotEmpty();
        assertThat(avtalerFunnet.getTotalElements()).isEqualTo(1);
        assertThat(avtalerFunnet.getContent().stream().findFirst().get().isFeilregistrert()).isFalse();
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_ufordelte_og_bryr_seg_ikke_om_nav_ident_ved_sok_pa_ufordelte() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtaleRepository.save(avtale5);

        Page<Avtale> resultat1 = avtaleRepository.sokEtterAvtale(null, null, null, null, ENHET_GEOGRAFISK.getVerdi(), null, null, true, PageRequest.of(0, 10));
        assertThat(resultat1.getContent()).hasSize(1);
        assertThat(resultat1.getContent().getFirst().getId()).isEqualTo(avtale1.getId());

        Page<Avtale> resultat2 = avtaleRepository.sokEtterAvtale(new NavIdent("A123456"), null, null, null, null, null, null, true, PageRequest.of(0, 10));
        assertThat(resultat2.getContent()).hasSize(1);
        assertThat(resultat2.getContent().getFirst().getId()).isEqualTo(avtale1.getId());
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_veileder() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtale1.setVeilederNavIdent(new NavIdent("A123456"));
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale2.setVeilederNavIdent(new NavIdent("B123456"));
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtale3.setVeilederNavIdent(new NavIdent("C123456"));
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtale4.setVeilederNavIdent(new NavIdent("D123456"));
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtale5.setVeilederNavIdent(new NavIdent("E123456"));
        avtaleRepository.save(avtale5);

        Page<Avtale> resultat = avtaleRepository.sokEtterAvtale(avtale3.getVeilederNavIdent(), null, null, null, null, null, null, false, PageRequest.of(0, 10));
        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().getFirst().getId()).isEqualTo(avtale3.getId());
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_avtaleNr() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtaleRepository.save(avtale5);

        Integer avtaleNr = avtaleRepository.findById(avtale5.getId()).map(Avtale::getAvtaleNr).orElse(null);
        Page<Avtale> resultat = avtaleRepository.sokEtterAvtale(null, avtaleNr, null, null, null, null, null, false, PageRequest.of(0, 10));
        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().getFirst().getId()).isEqualTo(avtale5.getId());
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_deltakerFnr() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtale1.setDeltakerFnr(Fnr.generer(2000, 1, 1));
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale2.setDeltakerFnr(Fnr.generer(2000, 2, 1));
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtale3.setDeltakerFnr(Fnr.generer(2000, 3, 1));
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtale4.setDeltakerFnr(Fnr.generer(2000, 4, 1));
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtale5.setDeltakerFnr(Fnr.generer(2000, 5, 1));
        avtaleRepository.save(avtale5);

        Page<Avtale> resultat = avtaleRepository.sokEtterAvtale(null, null, avtale4.getDeltakerFnr(), null, null, null, null, false, PageRequest.of(0, 10));
        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().getFirst().getId()).isEqualTo(avtale4.getId());
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_bedriftsNr() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtale1.setBedriftNr(new BedriftNr("100000001"));
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale2.setBedriftNr(new BedriftNr("100000002"));
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtale3.setBedriftNr(new BedriftNr("100000003"));
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtale4.setBedriftNr(new BedriftNr("100000004"));
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtale5.setBedriftNr(new BedriftNr("100000005"));
        avtaleRepository.save(avtale5);

        Page<Avtale> resultat = avtaleRepository.sokEtterAvtale(null, null, null, avtale2.getBedriftNr(), null, null, null, false, PageRequest.of(0, 10));
        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().getFirst().getId()).isEqualTo(avtale2.getId());
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_status_og_andre_parametere() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtale1.setVeilederNavIdent(new NavIdent("A123456"));
        avtale1.setStatus(Status.GJENNOMFØRES);
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enInkluderingstilskuddAvtale();
        avtale2.setVeilederNavIdent(new NavIdent("C123456"));
        avtale2.setStatus(Status.KLAR_FOR_OPPSTART);
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enAvtaleMedAltUtfylt();
        avtale3.setVeilederNavIdent(new NavIdent("D123456"));
        avtale3.setStatus(Status.PÅBEGYNT);
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enMentorAvtaleUsignert();
        avtale4.setVeilederNavIdent(new NavIdent("E123456"));
        avtale4.setStatus(Status.MANGLER_GODKJENNING);
        avtaleRepository.save(avtale4);

        Page<Avtale> resultat1 = avtaleRepository.sokEtterAvtale(null, null, null, null, null, null, Status.GJENNOMFØRES, false, PageRequest.of(0, 10));
        assertThat(resultat1.getContent()).hasSize(1);
        assertThat(resultat1.getContent().getFirst().getId()).isEqualTo(avtale1.getId());

        Page<Avtale> resultat2 = avtaleRepository.sokEtterAvtale(avtale2.getVeilederNavIdent(), null, null, null, null, null, Status.GJENNOMFØRES, false, PageRequest.of(0, 10));
        assertThat(resultat2.getContent()).hasSize(0);
    }

    @Test
    public void sokEtterAvtale_finner_avtaler_ved_sok_pa_tiltakstype_og_andre_parametere() {
        Avtale avtale1 = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet();
        avtale1.setVeilederNavIdent(new NavIdent("A123456"));
        avtaleRepository.save(avtale1);

        Avtale avtale2 = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        avtale2.setVeilederNavIdent(new NavIdent("B123456"));
        avtaleRepository.save(avtale2);

        Avtale avtale3 = TestData.enInkluderingstilskuddAvtale();
        avtale3.setVeilederNavIdent(new NavIdent("C123456"));
        avtaleRepository.save(avtale3);

        Avtale avtale4 = TestData.enAvtaleMedAltUtfylt();
        avtale4.setVeilederNavIdent(new NavIdent("D123456"));
        avtaleRepository.save(avtale4);

        Avtale avtale5 = TestData.enMentorAvtaleUsignert();
        avtale5.setVeilederNavIdent(new NavIdent("E123456"));
        avtaleRepository.save(avtale5);

        Page<Avtale> resultat1 = avtaleRepository.sokEtterAvtale(null, null, null, null, null, Tiltakstype.INKLUDERINGSTILSKUDD, null, false, PageRequest.of(0, 10));
        assertThat(resultat1.getContent()).hasSize(1);
        assertThat(resultat1.getContent().getFirst().getId()).isEqualTo(avtale3.getId());

        Page<Avtale> resultat2 = avtaleRepository.sokEtterAvtale(avtale3.getVeilederNavIdent(), null, null, null, null, Tiltakstype.INKLUDERINGSTILSKUDD, null, false, PageRequest.of(0, 10));
        assertThat(resultat2.getContent()).hasSize(1);
        assertThat(resultat1.getContent().getFirst().getId()).isEqualTo(avtale3.getId());
    }
}
