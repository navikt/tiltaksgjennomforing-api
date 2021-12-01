package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.*;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static java.util.Arrays.asList;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enArbeidstreningAvtale;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enNavIdent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class AvtaleControllerTest {

    @Mock
    VeilarbArenaClient veilarbArenaClient;
    @Mock
    Norg2Client norg2Client;
    @Spy
    TilskuddsperiodeConfig tilskuddsperiodeConfig = new TilskuddsperiodeConfig();
    @InjectMocks
    private AvtaleController avtaleController;
    @Mock
    private AvtaleRepository avtaleRepository;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private InnloggingService innloggingService;
    @Mock
    private EregService eregService;
    @Mock
    private PersondataService persondataService;
    @Mock
    private KontoregisterService kontoregisterService;

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
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = enArbeidstreningAvtale();
        Veileder veileder = new Veileder(TestData.enNavIdent(), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder.getIdentifikator()), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(hentetAvtale).isEqualTo(avtale);
    }

    @Test
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = TestData.enVeileder(avtale);
        værInnloggetSom(veileder);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER)).isExactlyInstanceOf(RessursFinnesIkkeException.class);
    }

    @Test
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetNavAnsattIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new Veileder(new NavIdent("Z333333"), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER)).isExactlyInstanceOf(TilgangskontrollException.class);
    }

    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_avtaler_dersom_veileder_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        Avtale avtaleForAnnenVeilder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), new NavIdent("Z111111"));
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(avtaleRepository.findAllByVeilederNavIdent(veilederNavIdent)).thenReturn(asList(avtaleForVeilederSomSøkesEtter, avtaleForAnnenVeilder));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(true);
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setVeilederNavIdent(veilederNavIdent);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(avtalePredicate.setVeilederNavIdent(veilederNavIdent), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler)
                .contains(avtaleForVeilederSomSøkesEtter)
                .doesNotContain(avtaleForAnnenVeilder);
    }

    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_tom_liste_dersom_veileder_ikke_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(avtaleRepository.findAllByVeilederNavIdent(veilederNavIdent)).thenReturn(List.of(avtaleForVeilederSomSøkesEtter));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(false);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setVeilederNavIdent(veilederNavIdent), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler).doesNotContain(avtaleForVeilederSomSøkesEtter);
    }

    @Test
    public void hentAvtalerOpprettetAvInnloggetVeileder_skal_returnere_avtaler_dersom_veileder_har_tilgang() {
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Avtale avtaleForInnloggetVeileder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), identTilInnloggetVeileder);
        Avtale avtaleForAnnenVeilder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), new NavIdent("Z111111"));
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(avtaleRepository.findAllByVeilederNavIdent(identTilInnloggetVeileder)).thenReturn(asList(avtaleForInnloggetVeileder, avtaleForAnnenVeilder));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(true);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setVeilederNavIdent(identTilInnloggetVeileder), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler)
                .contains(avtaleForInnloggetVeileder)
                .doesNotContain(avtaleForAnnenVeilder);
    }

    @Test
    public void hentAvtaleOpprettetAvInnloggetVeileder_fordelt_oppfolgingsEnhet_og_geoEnhet() {
        NavIdent navIdent = new NavIdent("Z123456");
        String navEnhet = "0904";
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        Avtale nyAvtaleMedGeografiskEnhet = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhetOgGeografiskEnhet();
        Avtale nyAvtaleMedOppfølgningsEnhet = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet();

        when(avtaleRepository.findAllFordelteOrUfordeltByEnhet(navEnhet)).thenReturn(asList(nyAvtaleMedGeografiskEnhet, nyAvtaleMedOppfølgningsEnhet));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), any(Fnr.class))).thenReturn(true);

        List<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setNavEnhet(navEnhet), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler).isNotNull();
        assertThat(avtaler)
                .contains(nyAvtaleMedGeografiskEnhet);

    }

    @Test
    public void hentAvtaleOpprettetAvInnloggetVeileder_pa_avtaleNr() {
        NavIdent navIdent = new NavIdent("Z123456");
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);

        Avtale enArbeidstreningsAvtale = TestData.enArbeidstreningAvtale();
        enArbeidstreningsAvtale.setAvtaleNr(TestData.ET_AVTALENR);

        when(avtaleRepository.findAllByAvtaleNr(TestData.ET_AVTALENR)).thenReturn(asList(enArbeidstreningsAvtale));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), any(Fnr.class))).thenReturn(true);

        List<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setAvtaleNr(TestData.ET_AVTALENR), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler).isNotNull();
        assertThat(avtaler).contains(enArbeidstreningsAvtale);

    }

    @Test
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetSelvbetjeningBrukerIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new Arbeidsgiver(new Fnr("55555566666"), Set.of(), Map.of(), null, null));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.hent(avtale.getId(), Avtalerolle.ARBEIDSGIVER)).isExactlyInstanceOf(TilgangskontrollException.class);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        when(eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(new Organisasjon(avtale.getBedriftNr(), avtale.getBedriftNavn()));
        ResponseEntity svar = avtaleController.opprettAvtaleSomVeileder(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getBedriftNr(), Tiltakstype.ARBEIDSTRENING));
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svar.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + avtale.getId());
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER)).isExactlyInstanceOf(RessursFinnesIkkeException.class);
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = new Veileder(enNavIdent(), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(any(NavIdent.class), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enArbeidsgiver());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.ARBEIDSGIVER)).isInstanceOf(TilgangskontrollException.class);
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.enArbeidstreningAvtale();
        Avtale avtaleUtenTilgang = Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("01039513753"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("X643564"));
        Deltaker deltaker = TestData.enDeltaker(avtaleMedTilgang);
        værInnloggetSom(deltaker);
        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));
        when(avtaleRepository.findAllByDeltakerFnr(deltaker.getIdentifikator())).thenReturn(alleAvtaler);
        var hentedeAvtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate(), Avtale.Fields.sistEndret, Avtalerolle.DELTAKER);
        assertThat(hentedeAvtaler)
                .hasSize(avtalerBrukerHarTilgangTil.size())
                .allMatch(deltaker::harTilgang);
    }

    @Test
    public void opprettAvtaleSomVeileder__skal_feile_hvis_veileder_ikke_har_tilgang_til_bruker() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        Veileder enNavAnsatt = new Veileder(new NavIdent("T000000"), tilgangskontrollService, persondataServiceIMetode, norg2Client,
                Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt.getIdentifikator(), deltakerFnr)).thenReturn(false);
        assertThatThrownBy(() -> avtaleController.opprettAvtaleSomVeileder(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING))).isInstanceOf(IkkeTilgangTilDeltakerException.class);
    }

    @Test
    public void opprettAvtaleSomVeileder__skal_feile_hvis_kode6() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        Veileder enNavAnsatt = new Veileder(new NavIdent("T000000"), tilgangskontrollService, persondataServiceIMetode, norg2Client,
                Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt.getIdentifikator(), deltakerFnr)).thenReturn(true);
        PdlRespons pdlRespons = TestData.enPdlrespons(true);
        when(persondataServiceIMetode.hentPersondata(deltakerFnr)).thenReturn(pdlRespons);
        when(persondataServiceIMetode.erKode6(pdlRespons)).thenCallRealMethod();
        assertThatThrownBy(() -> avtaleController.opprettAvtaleSomVeileder(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING))).isInstanceOf(KanIkkeOppretteAvtalePåKode6Exception.class);
    }

    @Test
    public void opprettAvtaleSomArbeidsgiver__skal_feile_hvis_ag_ikke_har_tilgang_til_bedrift() {
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(TestData.etFodselsnummer(), Set.of(), Map.of(), null, null);
        værInnloggetSom(arbeidsgiver);
        assertThatThrownBy(() -> avtaleController.opprettAvtaleSomArbeidsgiver(new OpprettAvtale(new Fnr("99887765432"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING))).isInstanceOf(TilgangskontrollException.class);
    }

    private void værInnloggetSom(Avtalepart avtalepart) {
        lenient().when(innloggingService.hentAvtalepart(any())).thenReturn(avtalepart);
        if (avtalepart instanceof Veileder) {
            lenient().when(innloggingService.hentVeileder()).thenReturn((Veileder) avtalepart);
        }
        if (avtalepart instanceof Arbeidsgiver) {
            lenient().when(innloggingService.hentArbeidsgiver()).thenReturn((Arbeidsgiver) avtalepart);
        }
    }

    //Tester er avhengig av tekster i AvtalePart class og subclasses
    @Test
    public void avtaleStatus__veileder_maa_fylleut_avtale_foer_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = TestData.enVeileder(avtale);
        værInnloggetSom(veileder);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__arbeidsgiver_maa_fylleut_avtale_foer_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.ARBEIDSGIVER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
        assertThat(avtaleStatusDetaljer.part1).isEqualTo("Deltaker har ikke godkjent");
        assertThat(avtaleStatusDetaljer.part2).isEqualTo("Veileder har ikke godkjent");
    }

    @Test
    public void avtaleStatus__deltaker_maa_be_om_utfylling_av_avtale_foer_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        værInnloggetSom(deltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Deltaker.tekstHeaderAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Deltaker.tekstAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");

    }

    @Test
    public void avtaleStatus__deltaker_og_arbeidsgiver_maa_godkjenne_avtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        værInnloggetSom(deltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Deltaker.tekstAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Deltaker.ekstraTekstAvtaleVenterPaaDinGodkjenning);

    }

    @Test
    public void avtaleStatus__arbeidsgiver__maa_godkjenn__avtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.ARBEIDSGIVER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Arbeidsgiver.tekstAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Arbeidsgiver.ekstraTekstAvtaleVenterPaaDinGodkjenning);
    }

    @Test
    public void avtaleStatus__veileder_maa_vente_paa_andre_parter_godkjenning_kan_godkjenne_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__veileder_maa_vente_paa_andre_parter_godkjenning_deltaker_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.godkjennForDeltaker(TestData.enDeltaker(avtale).getIdentifikator());
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__deltaker_og_arbeidsgiver_har_godkjent_avtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.godkjennForDeltaker(TestData.enDeltaker(avtale).getIdentifikator());
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver(avtale).getIdentifikator());
        var deltaker = TestData.enDeltaker(avtale);
        værInnloggetSom(deltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
        var arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__godkjent_av_alle_parter() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setStartDato(Now.localDate().plusWeeks(1));
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleErGodkjentAvAllePartner);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Avtalepart.tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(Avtalepart.formatter) + ".");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Veileder.ekstraTekstAvtleErGodkjentAvAllePartner);
    }

    @Test
    public void viser_ikke_avbruttGrunn_til_arbeidsgiver() {
        Avtale avtale = enArbeidstreningAvtale();
        avtale.setAvbruttGrunn("Hemmelig");
        var arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(hentetAvtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void viser_ikke_navenheter_til_arbeidsgiver() {
        Avtale avtale = enArbeidstreningAvtale();
        var arbeidsgiver = TestData.enArbeidsgiver(avtale);
        værInnloggetSom(arbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(hentetAvtale.getEnhetGeografisk()).isNull();
        assertThat(hentetAvtale.getEnhetOppfolging()).isNull();
    }


    @Test
    public void hentBedriftKontonummer_skal_returnere_nytt_bedriftKontonummer() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(kontoregisterService.hentKontonummer(anyString())).thenReturn("990983666");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        String kontonummer = avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(kontonummer).isEqualTo("990983666");
    }

    @Test
    public void hentBedriftKontonummer_skal_kaste_en_feil_når_innlogget_part_ikke_har_tilgang_til_Avtale() throws TilgangskontrollException {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(false);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER)).isInstanceOf(TilgangskontrollException.class);
    }

    @Test
    public void hentBedriftKontonummer_skal_kaste_en_feil_når_kontonummer_rest_service_svarer_med_feil_response_status_og_kaster_en_exception() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet(), new SlettemerkeProperties(), false);
        værInnloggetSom(veileder);
        when(kontoregisterService.hentKontonummer(anyString())).thenThrow(KontoregisterFeilException.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        assertThatThrownBy(() -> avtaleController.hentBedriftKontonummer(avtale.getId(), Avtalerolle.VEILEDER)).isInstanceOf(KontoregisterFeilException.class);
    }
}
