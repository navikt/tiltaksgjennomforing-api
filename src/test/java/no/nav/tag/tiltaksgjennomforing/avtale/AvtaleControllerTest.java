package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppretteAvtalePåKode6Eller7Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static java.util.Arrays.asList;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enArbeidstreningAvtale;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enNavIdent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
@RunWith(MockitoJUnitRunner.class)
public class AvtaleControllerTest {

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
    VeilarbArenaClient veilarbArenaClient;

    @Mock
    Norg2Client norg2Client;

    private static List<Avtale> lagListeMedAvtaler(Avtale avtale, int antall) {
        List<Avtale> avtaler = new ArrayList<>();
        for (int i = 0; i <= antall; i++) {
            avtaler.add(avtale);
        }
        return avtaler;
    }

    private static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("88888899999");
        BedriftNr bedriftNr = new BedriftNr("12345678");
        return new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING);
    }

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = enArbeidstreningAvtale();
        Veileder veileder = new Veileder(TestData.enNavIdent(), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder.getIdentifikator()), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(hentetAvtale).isEqualTo(avtale);
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = TestData.enVeileder(avtale);
        værInnloggetSom(veileder);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetNavAnsattIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new Veileder(new NavIdent("Z333333"), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
    }

    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_avtaler_dersom_veileder_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        Avtale avtaleForAnnenVeilder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), new NavIdent("Z111111"));
        NavIdent identTilInnloggetVeileder = new NavIdent("Z333333");
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
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
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
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
        Veileder veileder = new Veileder(identTilInnloggetVeileder, tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        værInnloggetSom(veileder);
        when(avtaleRepository.findAllByVeilederNavIdent(identTilInnloggetVeileder)).thenReturn(asList(avtaleForInnloggetVeileder, avtaleForAnnenVeilder));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(identTilInnloggetVeileder), any(Fnr.class))).thenReturn(true);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setVeilederNavIdent(identTilInnloggetVeileder), Avtale.Fields.sistEndret, Avtalerolle.VEILEDER);
        assertThat(avtaler)
                .contains(avtaleForInnloggetVeileder)
                .doesNotContain(avtaleForAnnenVeilder);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetSelvbetjeningBrukerIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new Arbeidsgiver(new Fnr("55555566666"), Set.of(), Map.of(), null, null));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId(), Avtalerolle.ARBEIDSGIVER);
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

    @Test(expected = RessursFinnesIkkeException.class)
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Veileder veileder = new Veileder(enNavIdent(), tilgangskontrollService, persondataService, norg2Client, Collections.emptySet());
        værInnloggetSom(veileder);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(any(NavIdent.class), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = TilgangskontrollException.class)
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enArbeidsgiver());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.enArbeidstreningAvtale();
        Avtale avtaleUtenTilgang = Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("89898989898"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("X643564"));
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

    @Test(expected = IkkeTilgangTilDeltakerException.class)
    public void opprettAvtaleSomVeileder__skal_feile_hvis_veileder_ikke_har_tilgang_til_bruker() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        Veileder enNavAnsatt = new Veileder(new NavIdent("T000000"), tilgangskontrollService, persondataServiceIMetode, norg2Client,
            Collections.emptySet());
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt.getIdentifikator(), deltakerFnr)).thenReturn(false);
        avtaleController.opprettAvtaleSomVeileder(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING));
    }

    @Test(expected = KanIkkeOppretteAvtalePåKode6Eller7Exception.class)
    public void opprettAvtaleSomVeileder__skal_feile_hvis_kode6() {
        PersondataService persondataServiceIMetode = mock(PersondataService.class);
        Veileder enNavAnsatt = new Veileder(new NavIdent("T000000"), tilgangskontrollService, persondataServiceIMetode, norg2Client,
            Collections.emptySet());
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(enNavAnsatt.getIdentifikator(), deltakerFnr)).thenReturn(true);
        PdlRespons pdlRespons = TestData.enPdlrespons(true);
        when(persondataServiceIMetode.hentPersondata(deltakerFnr)).thenReturn(pdlRespons);
        when(persondataServiceIMetode.erKode6Eller7(pdlRespons)).thenCallRealMethod();
        avtaleController.opprettAvtaleSomVeileder(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING));
    }

    @Test(expected = TilgangskontrollException.class)
    public void opprettAvtaleSomArbeidsgiver__skal_feile_hvis_ag_ikke_har_tilgang_til_bedrift() {
        Arbeidsgiver arbeidsgiver = new Arbeidsgiver(TestData.etFodselsnummer(), Set.of(), Map.of(), null, null);
        værInnloggetSom(arbeidsgiver);
        avtaleController.opprettAvtaleSomArbeidsgiver(new OpprettAvtale(new Fnr("99887765432"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING));
    }

    private void værInnloggetSom(Avtalepart avtalepart) {
        when(innloggingService.hentAvtalepart(any())).thenReturn(avtalepart);
        if (avtalepart instanceof Veileder) {
            when(innloggingService.hentVeileder()).thenReturn((Veileder) avtalepart);
        }
        if (avtalepart instanceof Arbeidsgiver) {
            when(innloggingService.hentArbeidsgiver()).thenReturn((Arbeidsgiver) avtalepart);
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
        avtale.setStartDato(LocalDate.now().plusWeeks(1));
        værInnloggetSom(TestData.enVeileder(avtale));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleErGodkjentAvAllePartner);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Avtalepart.tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(Avtalepart.formatter)+".");
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
}
