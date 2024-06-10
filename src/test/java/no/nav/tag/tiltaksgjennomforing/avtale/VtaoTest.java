package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleMeldingEntitetRepository;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class VtaoTest {

    @Autowired
    private VarselRepository varselRepository;
    @Autowired
    private AvtaleRepository avtaleRepository;
    @Autowired
    private VtaoRepository vtaoRepository;
    @Autowired
    private AvtaleController avtaleController;
    @Autowired
    private AvtaleMeldingEntitetRepository avtaleMeldingEntitetRepository;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InnloggingService innloggingService;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private PersondataService persondataService;
    @MockBean
    AxsysService axsysService;
    @Mock
    VeilarbArenaClient veilarbArenaClient;
    @Mock
    Norg2Client norg2Client;
    @Autowired
    private AvtaleInnholdRepository avtaleInnholdRepository;

    @BeforeEach
    public void setUp() {
        varselRepository.deleteAll();
        avtaleMeldingEntitetRepository.deleteAll();
        vtaoRepository.deleteAll();
        avtaleInnholdRepository.deleteAll();
        avtaleRepository.deleteAll();
    }

    @Test
    public void kanOppretteVtaoAvtaleTest() {
        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarbArenaClient
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // TODO: opprett avtale via endepunkt avtaleController.opprettAvtaleSomVeileder()
    }

    @Test
    public void kanOppretteOgEndreKontaktInfoForVtaoTest() {
        // Lagre en ny VTAO-avtale
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        var lagretAvtale = avtaleRepository.save(avtale);

        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarbArenaClient
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        // Ingrid Espelid: "Så juksar me lite"
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(LocalDateTime.now());
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(LocalDateTime.now());
        avtaleRepository.save(hentetAvtale);

        avtaleController.endreKontaktinfo(lagretAvtale.getId(), new EndreKontaktInformasjon(
                lagretAvtale.getGjeldendeInnhold().getDeltakerFornavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerTlf(),
                lagretAvtale.getGjeldendeInnhold().getVeilederFornavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederTlf(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverFornavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverTlf(),
                null,
                new VtaoFelter(lagretAvtale.getGjeldendeInnhold().getVtao())
        ));
        var endretAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        assertNotEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao(),
                endretAvtale.getGjeldendeInnhold().getVtao(),
                "Vtao har endret id og blitt duplisert");

        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().getFadderFornavn(),
                endretAvtale.getGjeldendeInnhold().getVtao().getFadderFornavn(),
                "Men verdier i felter er de samme (stikkprøve på fadderFornavn)");

        var versjoner = avtaleController.hentVersjoner(lagretAvtale.getId(), Avtalerolle.VEILEDER);
        assertEquals(versjoner.getFirst().getVtao(),
                lagretAvtale.getGjeldendeInnhold().getVtao(),
                "Første versjon er den samme som lagretAvtale");

        assertEquals(2, versjoner.size(), "Det er to versjoner av avtaleInnhold");
        var vtaoListe = vtaoRepository.findAll();
        assertEquals(2, vtaoListe.size(), "Det er også to versjoner av Vtao");
    }

    @Test
    public void kanEndreVtaoAvtaleUtenVtaoEndringerTest() {
        // Lagre en ny VTAO-avtale
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        var lagretAvtale = avtaleRepository.save(avtale);

        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarbArenaClient
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        // Ingrid Espelid: "Så juksar me lite"
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(null);
        avtaleRepository.save(hentetAvtale);
        //avtaleController.opphevGodkjenninger(lagretAvtale.getId(), Avtalerolle.VEILEDER);

        var endretData = new EndreAvtale(
                lagretAvtale.getGjeldendeInnhold().getDeltakerFornavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerTlf(),
                lagretAvtale.getGjeldendeInnhold().getBedriftNavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverFornavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverTlf(),
                lagretAvtale.getGjeldendeInnhold().getVeilederFornavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederTlf(),
                lagretAvtale.getGjeldendeInnhold().getOppfolging(),
                lagretAvtale.getGjeldendeInnhold().getTilrettelegging(),
                lagretAvtale.getGjeldendeInnhold().getStartDato(),
                lagretAvtale.getGjeldendeInnhold().getSluttDato(),
                lagretAvtale.getGjeldendeInnhold().getStillingprosent(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsoppgaver(),
                lagretAvtale.getGjeldendeInnhold().getStillingstittel(),
                lagretAvtale.getGjeldendeInnhold().getStillingStyrk08(),
                lagretAvtale.getGjeldendeInnhold().getStillingKonseptId(),
                lagretAvtale.getGjeldendeInnhold().getAntallDagerPerUke(),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonFornavn).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonEtternavn).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonTlf).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getØnskerVarslingOmRefusjon).orElse(null),
                lagretAvtale.getGjeldendeInnhold().getMaal(),
                lagretAvtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift(),
                lagretAvtale.getGjeldendeInnhold().getInkluderingstilskuddBegrunnelse(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverKontonummer(),
                lagretAvtale.getGjeldendeInnhold().getLonnstilskuddProsent(),
                lagretAvtale.getGjeldendeInnhold().getManedslonn(),
                lagretAvtale.getGjeldendeInnhold().getFeriepengesats(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiveravgift(),
                lagretAvtale.getGjeldendeInnhold().getOtpSats(),
                lagretAvtale.getGjeldendeInnhold().getHarFamilietilknytning(),
                lagretAvtale.getGjeldendeInnhold().getFamilietilknytningForklaring(),
                lagretAvtale.getGjeldendeInnhold().getStillingstype(),
                lagretAvtale.getGjeldendeInnhold().getMentorFornavn(),
                lagretAvtale.getGjeldendeInnhold().getMentorEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getMentorOppgaver(),
                lagretAvtale.getGjeldendeInnhold().getMentorAntallTimer(),
                lagretAvtale.getGjeldendeInnhold().getMentorTlf(),
                lagretAvtale.getGjeldendeInnhold().getMentorTimelonn(),
                lagretAvtale.getGjeldendeInnhold().getVtao().hentFelter()
        );

        avtaleController.endreAvtale(lagretAvtale.getId(), lagretAvtale.getSistEndret(), endretData, Avtalerolle.VEILEDER);
        var endretAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().getId(),
                endretAvtale.getGjeldendeInnhold().getVtao().getId(),
                "Vtao har samme ID (samme rad i databasen)"
        );

        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao(),
                endretAvtale.getGjeldendeInnhold().getVtao(),
                "Men entiten har ikke endret seg"
        );
        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().hentFelter(),
                endretAvtale.getGjeldendeInnhold().getVtao().hentFelter(),
                "Men innholdet har ikke endret seg"
        );

        var avtaleInnholdListe = avtaleInnholdRepository.findAll();
        assertEquals(1, avtaleInnholdListe.size());
        var vtaoListe = vtaoRepository.findAll();
        assertEquals(1, vtaoListe.size());
    }

    @Test
    public void kanEndreVtaoAvtaleMedVtaoEndringerTest() {
        // Lagre en ny VTAO-avtale
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        var lagretAvtale = avtaleRepository.save(avtale);

        var navIdent = TestData.enNavIdent();
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Collections.emptySet(),
                new SlettemerkeProperties(),
                false,
                veilarbArenaClient
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        // Ingrid Espelid: "Så juksar me lite"
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(null);
        avtaleRepository.save(hentetAvtale);
        //avtaleController.opphevGodkjenninger(lagretAvtale.getId(), Avtalerolle.VEILEDER);

        var endretVtao = new VtaoFelter(
                "Freddy",
                "Faddersen",
                "87654321"
        );

        var endretData = new EndreAvtale(
                lagretAvtale.getGjeldendeInnhold().getDeltakerFornavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getDeltakerTlf(),
                lagretAvtale.getGjeldendeInnhold().getBedriftNavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverFornavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverTlf(),
                lagretAvtale.getGjeldendeInnhold().getVeilederFornavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getVeilederTlf(),
                lagretAvtale.getGjeldendeInnhold().getOppfolging(),
                lagretAvtale.getGjeldendeInnhold().getTilrettelegging(),
                lagretAvtale.getGjeldendeInnhold().getStartDato(),
                lagretAvtale.getGjeldendeInnhold().getSluttDato(),
                lagretAvtale.getGjeldendeInnhold().getStillingprosent(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsoppgaver(),
                lagretAvtale.getGjeldendeInnhold().getStillingstittel(),
                lagretAvtale.getGjeldendeInnhold().getStillingStyrk08(),
                lagretAvtale.getGjeldendeInnhold().getStillingKonseptId(),
                lagretAvtale.getGjeldendeInnhold().getAntallDagerPerUke(),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonFornavn).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonEtternavn).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getRefusjonKontaktpersonTlf).orElse(null),
                Optional.ofNullable(lagretAvtale.getGjeldendeInnhold().getRefusjonKontaktperson()).map(RefusjonKontaktperson::getØnskerVarslingOmRefusjon).orElse(null),
                lagretAvtale.getGjeldendeInnhold().getMaal(),
                lagretAvtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift(),
                lagretAvtale.getGjeldendeInnhold().getInkluderingstilskuddBegrunnelse(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiverKontonummer(),
                lagretAvtale.getGjeldendeInnhold().getLonnstilskuddProsent(),
                lagretAvtale.getGjeldendeInnhold().getManedslonn(),
                lagretAvtale.getGjeldendeInnhold().getFeriepengesats(),
                lagretAvtale.getGjeldendeInnhold().getArbeidsgiveravgift(),
                lagretAvtale.getGjeldendeInnhold().getOtpSats(),
                lagretAvtale.getGjeldendeInnhold().getHarFamilietilknytning(),
                lagretAvtale.getGjeldendeInnhold().getFamilietilknytningForklaring(),
                lagretAvtale.getGjeldendeInnhold().getStillingstype(),
                lagretAvtale.getGjeldendeInnhold().getMentorFornavn(),
                lagretAvtale.getGjeldendeInnhold().getMentorEtternavn(),
                lagretAvtale.getGjeldendeInnhold().getMentorOppgaver(),
                lagretAvtale.getGjeldendeInnhold().getMentorAntallTimer(),
                lagretAvtale.getGjeldendeInnhold().getMentorTlf(),
                lagretAvtale.getGjeldendeInnhold().getMentorTimelonn(),
                endretVtao
        );

        avtaleController.endreAvtale(lagretAvtale.getId(), lagretAvtale.getSistEndret(), endretData, Avtalerolle.VEILEDER);
        var endretAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        assertEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().getId(),
                endretAvtale.getGjeldendeInnhold().getVtao().getId(),
                "Vtao har samme ID (samme rad i databasen)"
        );
        assertNotEquals(
                lagretAvtale.getGjeldendeInnhold().getVtao().hentFelter(),
                endretAvtale.getGjeldendeInnhold().getVtao().hentFelter(),
                "Men innholdet har endret seg"
        );

        var avtaleInnholdListe = avtaleInnholdRepository.findAll();
        assertEquals(1, avtaleInnholdListe.size());
        var vtaoListe = vtaoRepository.findAll();
        assertEquals(1, vtaoListe.size());
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
}
