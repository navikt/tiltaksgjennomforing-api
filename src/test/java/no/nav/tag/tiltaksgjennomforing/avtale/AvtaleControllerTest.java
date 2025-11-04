package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.Regelmotor;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilAvtaleException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Kode6SperretForOpprettelseOgEndringException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KontoregisterFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enArbeidstreningAvtale;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enNavIdent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles(Miljø.TEST)
@AutoConfigureMockMvc
public class AvtaleControllerTest {

    @MockBean
    VeilarboppfolgingService veilarboppfolgingService;
    @MockBean
    Norg2Client norg2Client;
    @Autowired
    private AvtaleController avtaleController;
    @MockBean
    private AvtaleRepository avtaleRepository;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @MockBean
    private InnloggingService innloggingService;
    @MockBean
    private EregService eregService;
    @MockBean
    private PersondataService persondataService;
    @MockBean
    private KontoregisterService kontoregisterService;
    @MockBean
    private FeatureToggleService featureToggleServiceMock;

    private Pageable pageable = PageRequest.of(0, 100);

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private static List<Avtale> lagListeMedAvtaler(Avtale avtale, int antall) {
        List<Avtale> avtaler = new ArrayList<>();
        for (int i = 0; i <= antall; i++) {
            avtaler.add(avtale);
        }
        return avtaler;
    }

    private static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("00000000000");
        BedriftNr bedriftNr = new BedriftNr("12345678");
        return new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING);
    }

    @Test
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = TestData.enVeileder(avtale);
        værInnloggetSom(veileder);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(
                () -> avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER, null)
        ).isExactlyInstanceOf(RessursFinnesIkkeException.class);
    }

    @Test
    public void hentSkalKasteExceptionHvisInnloggetNavAnsattIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder =
            new Veileder(
                new NavIdent("Z333333"),
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                eregService,
                mock(Regelmotor.class)
            );
        værInnloggetSom(
            veileder
        );
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(tilgangskontrollService.hentSkrivetilgang(
            veileder,
            avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC, "Ukjent tilgang"));
        assertThatThrownBy(
                () -> avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER, null)
        ).isExactlyInstanceOf(IkkeTilgangTilAvtaleException.class);
    }

    @Disabled("må skrives om")
    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_tom_liste_dersom_veileder_ikke_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.opprett(lagOpprettAvtale(), Avtaleopphav.VEILEDER, veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(
                identTilInnloggetVeileder,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);
        Avtale exampleAvtale = Avtale.builder()
                .veilederNavIdent(new NavIdent("Z222222"))
                .build();
        when(
                avtaleRepository.findAll(eq(Example.of(exampleAvtale)), eq(pageable))
        ).thenReturn(new PageImpl<>(List.of(avtaleForVeilederSomSøkesEtter)));

        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(veileder),
                any(Fnr.class)
        )).thenReturn(false);
        AvtaleQueryParameter avtalePredicate = new AvtaleQueryParameter();

        Page<BegrensetAvtale> avtalerPageResponse = veileder.hentBegrensedeAvtalerMedLesetilgang(
                avtaleRepository,
                avtalePredicate.setVeilederNavIdent(veilederNavIdent),
                pageable
        );

        List<UUID> avtaleIder = avtalerPageResponse.getContent()
            .stream()
            .map(BegrensetAvtale::id)
            .toList();

        assertThat(avtaleIder).doesNotContain(avtaleForVeilederSomSøkesEtter.getId());
    }

    @Disabled("må skrives om")
    @Test
    public void hentAvtaleOpprettetAvInnloggetVeileder_pa_avtaleNr() {
        NavIdent navIdent = new NavIdent("Z123456");
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);

        Avtale enArbeidstreningsAvtale = TestData.enArbeidstreningAvtale();
        enArbeidstreningsAvtale.setAvtaleNr(TestData.ET_AVTALENR);

        Avtale exampleAvtale = Avtale.builder()
                .avtaleNr(TestData.ET_AVTALENR)
                .build();
        when(
                avtaleRepository.findAll(eq(Example.of(exampleAvtale)), eq(pageable))
        ).thenReturn(new PageImpl<>(List.of(enArbeidstreningsAvtale)));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        Page<BegrensetAvtale> avtalerPageResponse = veileder.hentBegrensedeAvtalerMedLesetilgang(
                avtaleRepository,
                new AvtaleQueryParameter().setAvtaleNr(TestData.ET_AVTALENR),
                pageable
        );

        List<BegrensetAvtale> avtaler = avtalerPageResponse.getContent();
        assertThat(avtaler).isNotNull();
        assertThat(avtaler.stream().filter(avtaleMinimalListevisning -> avtaleMinimalListevisning.tiltakstype() == Tiltakstype.ARBEIDSTRENING).toList()).isNotNull();
    }

    @Test
    public void mentorGodkjennTaushetserklæring_når_innlogget_er_ikke_Mentor() {
        Avtale enMentorAvtale = TestData.enMentorAvtaleUsignert();
        NavIdent navIdent = new NavIdent("Z123456");
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);

        assertThatThrownBy(() ->
                avtaleController.mentorGodkjennTaushetserklæring(enMentorAvtale.getId(), Avtalerolle.DELTAKER, Now.instant())).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void mentorGodkjennTaushetserklæring_når_innlogget_er__Mentor() {
        Avtale enMentorAvtale = TestData.enMentorAvtaleUsignert();
        Mentor mentor = new Mentor(enMentorAvtale.getMentorFnr());
        værInnloggetSom(mentor);

        when(avtaleRepository.findById(enMentorAvtale.getId())).thenReturn(Optional.of(enMentorAvtale));

        avtaleController.mentorGodkjennTaushetserklæring(enMentorAvtale.getId(), Avtalerolle.DELTAKER, Now.instant());
    }

    @Test
    public void hentSkalKasteExceptionHvisInnloggetSelvbetjeningBrukerIkkeHarTilgang() {
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(
                new Arbeidsgiver(
                        Fnr.generer(1956, 7, 8),
                        Set.of(),
                        Map.of(),
                        List.of(),
                        persondataService,
                        null,
                        null,
                        null,null
                )
        );
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(
                () -> avtaleController.hent(avtale.getId(), Avtalerolle.ARBEIDSGIVER, null)
        ).isExactlyInstanceOf(IkkeTilgangTilAvtaleException.class);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();

        EregService eregService  = mock(EregService.class);

        NavIdent navIdent = new NavIdent("Z123456");
        NavEnhet navEnhet = TestData.ENHET_OPPFØLGING;
        OpprettAvtale opprettAvtale = new OpprettAvtale(
                avtale.getDeltakerFnr(),
                avtale.getBedriftNr(),
                Tiltakstype.ARBEIDSTRENING
        );
        var veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(navEnhet),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                eregService,
                mock(Regelmotor.class)
        );

        værInnloggetSom(veileder);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        when(
                eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(
                new Organisasjon(
                        avtale.getBedriftNr(),
                        avtale.getGjeldendeInnhold().getBedriftNavn()
                )
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(persondataService.hentNavn(any(Fnr.class))).thenReturn(Navn.TOMT_NAVN);
        when(norg2Client.hentGeografiskEnhet(any()))
                .thenReturn(
                        new Norg2GeoResponse(
                                TestData.ENHET_GEOGRAFISK.getNavn(),
                                TestData.ENHET_GEOGRAFISK.getVerdi()
                        )
                );
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(any()))
                .thenReturn(
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS,
                                "0906"
                        )
                );

        ResponseEntity svar = avtaleController.opprettAvtaleSomVeileder(opprettAvtale);
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svar.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + avtale.getId());
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(
                () -> avtaleController.endreAvtale(
                        avtale.getId(),
                        TestData.ingenEndring(),
                        Avtalerolle.VEILEDER,
                        avtale.getSistEndret()
                )
        ).isExactlyInstanceOf(RessursFinnesIkkeException.class);
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = new Veileder(
                enNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                any(Veileder.class),
                any(Fnr.class)
        )).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(tilgangskontrollService.hentSkrivetilgang(
            veileder,
            avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Tillat());
        ResponseEntity svar = avtaleController.endreAvtale(
                avtale.getId(),
                TestData.ingenEndring(),
                Avtalerolle.VEILEDER,
                avtale.getSistEndret()
        );
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enArbeidsgiver());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(
                () -> avtaleController.endreAvtale(
                        avtale.getId(),
                        TestData.ingenEndring(),
                        Avtalerolle.ARBEIDSGIVER,
                        avtale.getSistEndret()
                )
        ).isInstanceOf(IkkeTilgangTilAvtaleException.class);
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.enArbeidstreningAvtale();
        Avtale avtaleUtenTilgang = Avtale.opprett(
                new OpprettAvtale(new Fnr("01039513753"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING),
                Avtaleopphav.VEILEDER,
                new NavIdent("X643564")
        );
        Deltaker deltaker = TestData.enDeltaker(avtaleMedTilgang);
        værInnloggetSom(deltaker);
        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));
        when(avtaleRepository.findAllByDeltakerFnrAndFeilregistrertIsFalse(eq(deltaker.getIdentifikator()), eq(pageable))).thenReturn(new PageImpl<>(alleAvtaler));

        Page<BegrensetAvtale> avtalerPageResponse = deltaker.hentBegrensedeAvtalerMedLesetilgang(
                avtaleRepository,
                new AvtaleQueryParameter(),
                pageable
        );

        List<BegrensetAvtale> avtaler = avtalerPageResponse.getContent();
        assertThat(avtaler).hasSize(avtalerBrukerHarTilgangTil.size());
    }

    @Test
    public void opprettAvtaleSomVeileder__skal_feile_hvis_veileder_ikke_har_tilgang_til_bruker_med_togglet_adressesperresjekk() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        when(featureToggleServiceMock.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(true);
        Veileder enNavAnsatt = new Veileder(
                new NavIdent("T000000"),
                null,
                tilgangskontrollService,
                persondataServiceIMetode,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = Fnr.generer(1978, 9, 10);
        when(
                tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr)
        ).thenReturn(true);
        when(
                tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr)
        ).thenReturn(false);
        assertThatThrownBy(
                () -> avtaleController.opprettAvtaleSomVeileder(
                        new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING)

                )
        ).isInstanceOf(IkkeTilgangTilDeltakerException.class);
    }

    @Test
    public void opprettAvtaleSomVeileder__skal_fungere_hvis_veileder_har_tilgang_til_bruker_uten_togglet_adressesperresjekk() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        when(featureToggleServiceMock.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(false);
        Veileder enNavAnsatt = new Veileder(
                new NavIdent("T000000"),
                null,
                tilgangskontrollService,
                persondataServiceIMetode,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = Fnr.generer(1956, 7, 8);
        when(
                tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr)
        ).thenReturn(false);
        assertThatThrownBy(
                () -> avtaleController.opprettAvtaleSomVeileder(
                        new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING)

                )
        ).isInstanceOf(IkkeTilgangTilDeltakerException.class);
    }

    @Test
    public void opprettAvtaleSomVeileder__skal_feile_hvis_kode6_med_togglet_adressesperresjekk() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        when(featureToggleServiceMock.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(true);
        Veileder enNavAnsatt = new Veileder(
                new NavIdent("T000000"),
                null,
                tilgangskontrollService,
                persondataServiceIMetode,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = Fnr.generer(1978, 9, 10);
        when(
                tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr)
        ).thenReturn(true);
        when(persondataServiceIMetode.hentDiskresjonskode(deltakerFnr)).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        assertThatThrownBy(
                () -> avtaleController.opprettAvtaleSomVeileder(
                        new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING)
                )
         ).isInstanceOf(Kode6SperretForOpprettelseOgEndringException.class);
    }

    @Test
    public void opprettAvtaleSomArbeidsgiver__skal_feile_hvis_ag_ikke_har_tilgang_til_bedrift() {
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(),
                Map.of(),
                null,
                null,
                null,
                null,
                null,null
        );
        værInnloggetSom(arbeidsgiver);
        assertThatThrownBy(
                () -> avtaleController.opprettAvtaleSomArbeidsgiver(
                        new OpprettAvtale(Fnr.generer(1978, 9, 10), new BedriftNr("111222333"),
                                Tiltakstype.ARBEIDSTRENING)
                )
        ).isInstanceOf(TilgangskontrollException.class);
    }

    private void værInnloggetSom(Avtalepart<?> avtalepart) {
        lenient().when(innloggingService.hentAvtalepart(any())).thenReturn(avtalepart);
        if (avtalepart instanceof Veileder veileder) {
            lenient().when(innloggingService.hentVeileder()).thenReturn(veileder);
        }
        if (avtalepart instanceof Arbeidsgiver arbeidsgiver) {
            lenient().when(innloggingService.hentArbeidsgiver()).thenReturn(arbeidsgiver);
        }
        if (avtalepart instanceof Beslutter beslutter) {
            lenient().when(innloggingService.hentBeslutter()).thenReturn(beslutter);
        }
    }

    @Test
    public void viser_ikke_navenheter_til_arbeidsgiver() {
        Avtale avtale = enArbeidstreningAvtale();
        var arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER, null);
        assertThat(hentetAvtale.getEnhetGeografisk()).isNull();
        assertThat(hentetAvtale.getEnhetOppfolging()).isNull();
    }


    @Test
    public void hentBedriftKontonummer_skal_returnere_nytt_bedriftKontonummer() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(), Avtaleopphav.VEILEDER, veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(
                identTilInnloggetVeileder,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);
        when(kontoregisterService.hentKontonummer(anyString())).thenReturn("990983666");
        when(
                tilgangskontrollService.harSkrivetilgangTilKandidat(
                        eq(veileder),
                        any(Fnr.class)
                )
        ).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(tilgangskontrollService.hentSkrivetilgang(
            veileder,
            avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Tillat());
        String kontonummer = avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(kontonummer).isEqualTo("990983666");
    }

    @Test
    public void hentBedriftKontonummer_skal_kaste_en_feil_når_innlogget_part_ikke_har_tilgang_til_Avtale() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(), Avtaleopphav.VEILEDER, veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(
                identTilInnloggetVeileder,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(veileder),
                any(Fnr.class)
        )).thenReturn(false);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(tilgangskontrollService.hentSkrivetilgang(
            veileder,
            avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Avvis(Avslagskode.IKKE_TILGANG_FRA_ABAC, "Ukjent tilgang"));
        assertThatThrownBy(
                () -> avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER)
        ).isInstanceOf(IkkeTilgangTilAvtaleException.class);
    }

    @Test
    public void hentBedriftKontonummer_skal_kaste_en_feil_når_kontonummer_rest_service_svarer_med_feil_response_status_og_kaster_en_exception() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(), Avtaleopphav.VEILEDER, veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(
                identTilInnloggetVeileder,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.hentSkrivetilgang(
            veileder,
            avtale.getDeltakerFnr())
        ).thenReturn(new Tilgang.Tillat());
        when(kontoregisterService.hentKontonummer(anyString())).thenThrow(KontoregisterFeilException.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(veileder),
                any(Fnr.class)
        )).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(
                () -> avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER)
        ).isInstanceOf(KontoregisterFeilException.class);
    }

    @Test
    public void godkjennForAvtalepart__skal_ikke_fungere_hvis_versjon_er_feil() {
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(
            identTilInnloggetVeileder,
            null,
            tilgangskontrollService,
            persondataService,
            norg2Client,
            Collections.emptySet(),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            featureToggleServiceMock,
            mock(EregService.class),mock(Regelmotor.class)
        );
        værInnloggetSom(veileder);

        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.godkjenn(
            avtale.getId(),
            Avtalerolle.VEILEDER,
            Instant.now().minusSeconds(60)
        )).isInstanceOf(SamtidigeEndringerException.class);
    }
}
