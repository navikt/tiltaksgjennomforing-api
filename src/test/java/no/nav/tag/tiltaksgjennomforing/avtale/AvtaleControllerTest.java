package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.*;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.Navn;
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
import static java.util.Collections.emptyList;
import static no.nav.tag.tiltaksgjennomforing.TestData.enArbeidstreningAvtale;
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
        InnloggetVeileder innloggetVeileder = innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService);
        værInnloggetSom(innloggetVeileder);
        when(tilgangskontrollService.harLesetilgangTilKandidat(eq(innloggetVeileder), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(hentetAvtale).isEqualTo(avtale);
    }

    private static InnloggetVeileder innloggetNavAnsatt(Avtalepart<NavIdent> avtalepartMedNavIdent, TilgangskontrollService tilgangskontrollService) {
        return new InnloggetVeileder(avtalepartMedNavIdent.getIdentifikator(), tilgangskontrollService);
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetNavAnsattIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new InnloggetVeileder(new NavIdent("Z333333"), tilgangskontrollService));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId(), Avtalerolle.VEILEDER);
    }

    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_avtaler_dersom_veileder_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        Avtale avtaleForAnnenVeilder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), new NavIdent("Z111111"));
        InnloggetVeileder innloggetBruker = new InnloggetVeileder(new NavIdent("Z333333"), tilgangskontrollService);
        værInnloggetSom(innloggetBruker);
        when(avtaleRepository.findAllByVeilederNavIdent(veilederNavIdent)).thenReturn(asList(avtaleForVeilederSomSøkesEtter, avtaleForAnnenVeilder));
        when(tilgangskontrollService.harLesetilgangTilKandidat(eq(innloggetBruker), any(Fnr.class))).thenReturn(true);
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setVeilederNavIdent(veilederNavIdent);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(avtalePredicate.setVeilederNavIdent(veilederNavIdent), Avtalerolle.VEILEDER);
        assertThat(avtaler)
                .contains(avtaleForVeilederSomSøkesEtter)
                .doesNotContain(avtaleForAnnenVeilder);
    }

    @Test
    public void hentAvtalerOpprettetAvVeileder_skal_returnere_tom_liste_dersom_veileder_ikke_har_tilgang() {
        NavIdent veilederNavIdent = new NavIdent("Z222222");
        Avtale avtaleForVeilederSomSøkesEtter = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), veilederNavIdent);
        InnloggetVeileder innloggetBruker = new InnloggetVeileder(new NavIdent("Z333333"), tilgangskontrollService);
        værInnloggetSom(innloggetBruker);
        when(avtaleRepository.findAllByVeilederNavIdent(veilederNavIdent)).thenReturn(List.of(avtaleForVeilederSomSøkesEtter));
        when(tilgangskontrollService.harLesetilgangTilKandidat(eq(innloggetBruker), any(Fnr.class))).thenReturn(false);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setVeilederNavIdent(veilederNavIdent), Avtalerolle.VEILEDER);
        assertThat(avtaler).doesNotContain(avtaleForVeilederSomSøkesEtter);
    }

    @Test
    public void hentAvtalerOpprettetAvInnloggetVeileder_skal_returnere_avtaler_dersom_veileder_har_tilgang() {
        NavIdent navIdent = new NavIdent("Z333333");
        Avtale avtaleForInnloggetVeileder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), navIdent);
        Avtale avtaleForAnnenVeilder = Avtale.veilederOppretterAvtale(lagOpprettAvtale(), new NavIdent("Z111111"));
        InnloggetVeileder innloggetVeileder = new InnloggetVeileder(navIdent, tilgangskontrollService);
        værInnloggetSom(innloggetVeileder);
        when(avtaleRepository.findAllByVeilederNavIdent(navIdent)).thenReturn(asList(avtaleForInnloggetVeileder, avtaleForAnnenVeilder));
        when(tilgangskontrollService.harLesetilgangTilKandidat(eq(innloggetVeileder), any(Fnr.class))).thenReturn(true);
        Iterable<Avtale> avtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate().setVeilederNavIdent(navIdent), Avtalerolle.VEILEDER);
        assertThat(avtaler)
                .contains(avtaleForInnloggetVeileder)
                .doesNotContain(avtaleForAnnenVeilder);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetSelvbetjeningBrukerIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(new InnloggetArbeidsgiver(new Fnr("55555566666"), Map.of(), Set.of()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId(), Avtalerolle.DELTAKER);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        when(eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(new Organisasjon(avtale.getBedriftNr(), avtale.getBedriftNavn()));
        when(persondataService.hentNavn(any())).thenReturn(Navn.TOMT_NAVN);
        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getBedriftNr(), Tiltakstype.ARBEIDSTRENING));
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svar.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + avtale.getId());
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(any(InnloggetVeileder.class), any(Fnr.class))).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = TilgangskontrollException.class)
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enInnloggetArbeidsgiver());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.endreAvtale(avtale.getId(), avtale.getSistEndret(), TestData.ingenEndring(), Avtalerolle.ARBEIDSGIVER);
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.enArbeidstreningAvtale();
        Avtale avtaleUtenTilgang = Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("89898989898"), new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("X643564"));
        InnloggetDeltaker innloggetDeltaker = TestData.innloggetDeltaker(TestData.enDeltaker(avtaleMedTilgang));
        værInnloggetSom(innloggetDeltaker);
        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));
        when(avtaleRepository.findAllByDeltakerFnr(innloggetDeltaker.getIdentifikator())).thenReturn(alleAvtaler);
        var hentedeAvtaler = avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil(new AvtalePredicate(), Avtalerolle.DELTAKER);
        assertThat(hentedeAvtaler)
                .hasSize(avtalerBrukerHarTilgangTil.size())
                .allMatch(innloggetDeltaker::harLeseTilgang);
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void hentRolleSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hentRolle(avtale.getId(), Avtalerolle.ARBEIDSGIVER);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentRolleSkalReturnereForbiddenHvisIkkeTilknyttetAvtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        værInnloggetSom(TestData.enInnloggetVeileder());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hentRolle(avtale.getId(), Avtalerolle.VEILEDER);
    }

    @Test
    public void hentRolleSkalReturnereOkMedEnRolleHvisInnloggetBrukerErTilknyttetAvtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetDeltaker selvbetjeningBruker = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        værInnloggetSom(selvbetjeningBruker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtalerolle svar = avtaleController.hentRolle(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(svar).isEqualTo(Avtalerolle.DELTAKER);
    }

    @Test(expected = TilgangskontrollException.class)
    public void opprettAvtale__skal_feile_hvis_veileder_ikke_har_tilgang_til_bruker() {
        InnloggetVeileder enNavAnsatt = TestData.enInnloggetVeileder();
        værInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        doThrow(TilgangskontrollException.class).when(tilgangskontrollService).sjekkSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr);
        avtaleController.opprettAvtale(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING));
    }

    private void værInnloggetSom(InnloggetBruker innloggetBruker) {
        when(innloggingService.hentInnloggetBruker(any())).thenReturn(innloggetBruker);
        if (innloggetBruker instanceof InnloggetVeileder) {
            when(innloggingService.hentInnloggetVeileder()).thenReturn((InnloggetVeileder) innloggetBruker);
        }
    }

    //Tester er avhengig av tekster i AvtalePart class og subclasses
    @Test
    public void avtaleStatus__veileder_maa_fylleut_avtale_foer_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetBruker enNavAnsatt = TestData.enInnloggetVeileder();
        værInnloggetSom(enNavAnsatt);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__arbeidsgiver_maa_fylleut_avtale_foer_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        InnloggetBruker innloggetArbeidsgiver = TestData.innloggetArbeidsgiver(TestData.enArbeidsgiver(avtale));
        værInnloggetSom(innloggetArbeidsgiver);
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
        InnloggetBruker innloggetDeltaker = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        værInnloggetSom(innloggetDeltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Deltaker.tekstHeaderAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Deltaker.tekstAvtalePaabegynt);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");

    }

    @Test
    public void avtaleStatus__deltaker_og_arbeidsgiver_maa_godkjenne_avtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        InnloggetBruker innloggetDeltaker = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        værInnloggetSom(innloggetDeltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Deltaker.tekstAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Deltaker.ekstraTekstAvtaleVenterPaaDinGodkjenning);

    }

    @Test
    public void avtaleStatus__arbeidsgiver__maa_godkjenn__avtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        InnloggetBruker innloggetArbeidsgiver = TestData.innloggetArbeidsgiver(TestData.enArbeidsgiver(avtale));
        værInnloggetSom(innloggetArbeidsgiver);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.ARBEIDSGIVER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Arbeidsgiver.tekstAvtaleVenterPaaDinGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Arbeidsgiver.ekstraTekstAvtaleVenterPaaDinGodkjenning);
    }

    @Test
    public void avtaleStatus__veileder_maa_vente_paa_andre_parter_godkjenning_kan_godkjenne_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
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
        værInnloggetSom(innloggetNavAnsatt(TestData.enVeileder(avtale), tilgangskontrollService));
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
        InnloggetBruker innloggetDeltaker = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        værInnloggetSom(innloggetDeltaker);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.DELTAKER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
        InnloggetBruker innloggetArbeidsgiver = TestData.innloggetDeltaker(TestData.enDeltaker(avtale));
        værInnloggetSom(innloggetArbeidsgiver);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderVentAndreGodkjenning);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo("");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo("");
    }

    @Test
    public void avtaleStatus__godkjent_av_alle_parter() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setStartDato(LocalDate.now().plusWeeks(1));
        værInnloggetSom(TestData.enInnloggetVeileder());
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        AvtaleStatusDetaljer avtaleStatusDetaljer = avtaleController.hentAvtaleStatusDetaljer(avtale.getId(), Avtalerolle.VEILEDER);
        assertThat(avtaleStatusDetaljer.header).isEqualTo(Avtalepart.tekstHeaderAvtaleErGodkjentAvAllePartner);
        assertThat(avtaleStatusDetaljer.infoDel1).isEqualTo(Avtalepart.tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(Avtalepart.formatter)+".");
        assertThat(avtaleStatusDetaljer.infoDel2).isEqualTo(Veileder.ekstraTekstAvtleErGodkjentAvAllePartner);
    }

}
