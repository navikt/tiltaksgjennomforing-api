package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ActiveProfiles(Miljø.TEST)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class VtaoTest {
    @Autowired
    private AvtaleRepository avtaleRepository;
    @Autowired
    private VtaoRepository vtaoRepository;
    @Autowired
    private AvtaleController avtaleController;
    @MockBean
    private InnloggingService innloggingService;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private PersondataService persondataService;
    @Mock
    private VeilarboppfolgingService veilarboppfolgingService;
    @Mock
    private Norg2Client norg2Client;
    @Autowired
    private AvtaleInnholdRepository avtaleInnholdRepository;

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
                veilarboppfolgingService
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
                veilarboppfolgingService
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        // Ingrid Espelid: "Så juksar me lite"
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
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
                veilarboppfolgingService
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentPersondata(any(Fnr.class))).thenReturn(new PdlRespons(null));

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(null);
        avtaleRepository.save(hentetAvtale);

        var endretData = EndreAvtale.fraAvtale(avtale);

        avtaleController.endreAvtale(lagretAvtale.getId(), hentetAvtale.getSistEndret(), endretData, Avtalerolle.VEILEDER);
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

        var avtaleInnholdListe = avtaleInnholdRepository.findAllByAvtale(lagretAvtale);
        assertEquals(1, avtaleInnholdListe.size());
        var vtaoListe = vtaoRepository.findAll();
        var avtaleInnholdIdSet = avtaleInnholdListe.stream().map(AvtaleInnhold::getId).collect(Collectors.toSet());
        assertEquals(1, vtaoListe.stream()
                .filter(x -> avtaleInnholdIdSet.contains(x.getAvtaleInnhold().getId()))
                .toList()
                .size());
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
            veilarboppfolgingService
        );
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentPersondata(any(Fnr.class))).thenReturn(new PdlRespons(null));

        // Hent avtalen fra databasen
        var hentetAvtale = avtaleController.hent(lagretAvtale.getId(), Avtalerolle.VEILEDER, null);

        // Ingrid Espelid: "Så juksar me lite"
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvVeileder(null);
        hentetAvtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(null);
        avtaleRepository.save(hentetAvtale);

        var endretVtao = new VtaoFelter(
                "Freddy",
                "Faddersen",
                "87654321"
        );

        var endretData = EndreAvtale.fraAvtale(lagretAvtale);
        endretData.setVtao(endretVtao);

        avtaleController.endreAvtale(lagretAvtale.getId(), hentetAvtale.getSistEndret(), endretData, Avtalerolle.VEILEDER);
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

        var avtaleInnholdListe = avtaleInnholdRepository.findAllByAvtale(lagretAvtale);
        assertEquals(1, avtaleInnholdListe.size());
        var vtaoListe = vtaoRepository.findAll();
        var avtaleInnholdIdSet = avtaleInnholdListe.stream().map(AvtaleInnhold::getId).collect(Collectors.toSet());
        assertEquals(1, vtaoListe.stream()
                .filter(x -> avtaleInnholdIdSet.contains(x.getAvtaleInnhold().getId()))
                .toList()
                .size());
    }

    @Test
    public void måBesluttesForåGodkjennes() {
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(null);
        var navIdent = avtale.getVeilederNavIdent();
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = new GodkjentPaVegneGrunn();
        godkjentPaVegneGrunn.setDigitalKompetanse(true);
        avtale.godkjennForVeilederOgDeltaker(navIdent, godkjentPaVegneGrunn);
        assertEquals(avtale.getStatus(), Status.MANGLER_GODKJENNING);
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
