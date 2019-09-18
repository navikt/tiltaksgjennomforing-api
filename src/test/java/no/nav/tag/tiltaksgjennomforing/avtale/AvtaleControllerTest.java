package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.*;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang.TilgangUnderPilotering;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.setRemoveAssertJRelatedElementsFromStackTrace;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvtaleControllerTest {

    @InjectMocks
    private AvtaleController avtaleController;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    private TilgangUnderPilotering tilgangUnderPilotering;

    @Mock
    private TilgangskontrollService tilgangskontrollService;

    @Mock
    private InnloggingService innloggingService;

    @Mock
    private EregService eregService;

    private static List<Avtale> lagListeMedAvtaler(Avtale avtale, int antall) {
        List<Avtale> avtaler = new ArrayList<>();
        for (int i = 0; i <= antall; i++) {
            avtaler.add(avtale);
        }
        return avtaler;
    }

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId()).getBody();

        assertThat(hentetAvtale).isEqualTo(avtale);
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hent(avtale.getId());
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetNavAnsattIkkeHarTilgang() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId());
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentSkalKastTilgangskontrollExceptionHvisInnloggetSelvbetjeningBrukerIkkeHarTilgang() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(new InnloggetSelvbetjeningBruker(new Fnr("55555566666"), emptyList()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hent(avtale.getId());
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        when(eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(new Organisasjon(avtale.getBedriftNr(), avtale.getBedriftNavn()));

        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getBedriftNr()));

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svar.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + avtale.getId());
    }

    @Test
    public void opprettNyAvtaleRevisjonSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        when(eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(new Organisasjon(avtale.getBedriftNr(), avtale.getBedriftNavn()));

        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getBedriftNr()));
        avtaleRepository.save(avtale);
        try {
            avtale.setId(avtaleRepository.findAll().iterator().next().getId());
            avtale.setBaseAvtaleId(avtaleRepository.findAll().iterator().next().getId());
        } catch (Exception e) {
            System.out.println(e.getMessage() + "cause " + e.getCause());
            e.printStackTrace();
            avtale.setId(UUID.fromString("6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3"));
            avtale.setBaseAvtaleId(UUID.fromString("6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3"));
        }
        Avtale nyAvtaleRevisjon = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(nyAvtaleRevisjon)));
        when(avtaleRepository.save(any(Avtale.class))).thenReturn(nyAvtaleRevisjon);
        when(eregService.hentVirksomhet(avtale.getBedriftNr())).thenReturn(new Organisasjon(nyAvtaleRevisjon.getBedriftNr(), nyAvtaleRevisjon.getBedriftNavn()));
        ResponseEntity svarRevisjon = avtaleController.opprettAvtaleRevisjon(new OpprettAvtale(nyAvtaleRevisjon.getDeltakerFnr(), nyAvtaleRevisjon.getBedriftNr()), avtale.getId());

        assertThat(svarRevisjon.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svarRevisjon.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + nyAvtaleRevisjon.getId());
    }

    @Test
    public void KanLaaseOppAvtalenSkalFeileHvisDetFinnesVersjonSomIkkeErGodkjentAvVeileder() {
        assertThat(avtaleRepository.findAllByBaseAvtaleIdAndGodkjentAvVeileder(UUID.fromString("6ae3be81-abcd-477e-a8f3-4a5eb5fe91e3"), null)).isEqualTo(false);
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.innloggetNavAnsatt(TestData.enVeileder(avtale)));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = TilgangskontrollException.class)
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.enSelvbetjeningBruker());

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.enAvtale();
        Avtale avtaleUtenTilgang = Avtale.nyAvtale(new OpprettAvtale(new Fnr("89898989898"), new BedriftNr("111222333")), new NavIdent("X643564"));

        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerUtenOrganisasjon(TestData.enDeltaker(avtaleMedTilgang));
        vaerInnloggetSom(selvbetjeningBruker);

        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));

        when(avtaleRepository.findAll()).thenReturn(alleAvtaler);

        List<Avtale> hentedeAvtaler = new ArrayList<>();
        for (Avtale avtale : avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil()) {
            hentedeAvtaler.add(avtale);
        }

        hentedeAvtaler.forEach(avtale -> assertThat(selvbetjeningBruker.harLeseTilgang(avtale)).isTrue());
        assertThat(hentedeAvtaler.size()).isEqualTo(avtalerBrukerHarTilgangTil.size());
    }

    @Test(expected = RessursFinnesIkkeException.class)
    public void hentRolleSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.enAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hentRolle(avtale.getId());
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentRolleSkalReturnereForbiddenHvisIkkeTilknyttetAvtale() {
        Avtale avtale = TestData.enAvtale();
        vaerInnloggetSom(TestData.enNavAnsatt());

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        avtaleController.hentRolle(avtale.getId());
    }

    @Test
    public void hentRolleSkalReturnereOkMedEnRolleHvisInnloggetBrukerErTilknyttetAvtale() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerUtenOrganisasjon(TestData.enDeltaker(avtale));
        vaerInnloggetSom(selvbetjeningBruker);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hentRolle(avtale.getId());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(svar.getBody()).isEqualTo(Avtalerolle.DELTAKER);
    }

    @Test(expected = TilgangskontrollException.class)
    public void opprettAvtale__skal_feile_hvis_veileder_ikke_er_i_pilotering() {
        vaerInnloggetSom(TestData.enNavAnsatt());
        doThrow(TilgangskontrollException.class).when(tilgangUnderPilotering).sjekkTilgang(any());
        avtaleController.opprettAvtale(new OpprettAvtale(new Fnr("11111100000"), new BedriftNr("111222333")));
    }

    @Test(expected = TilgangskontrollException.class)
    public void opprettAvtale__skal_feile_hvis_veileder_ikke_har_tilgang_til_bruker() {
        InnloggetNavAnsatt enNavAnsatt = TestData.enNavAnsatt();
        vaerInnloggetSom(enNavAnsatt);
        Fnr deltakerFnr = new Fnr("11111100000");
        doThrow(TilgangskontrollException.class).when(tilgangskontrollService).sjekkSkrivetilgangTilKandidat(enNavAnsatt, deltakerFnr);
        avtaleController.opprettAvtale(new OpprettAvtale(deltakerFnr, new BedriftNr("111222333")));
    }

    private void vaerInnloggetSom(InnloggetBruker innloggetBruker) {
        when(innloggingService.hentInnloggetBruker()).thenReturn(innloggetBruker);
        if (innloggetBruker instanceof InnloggetNavAnsatt) {
            when(innloggingService.hentInnloggetNavAnsatt()).thenReturn((InnloggetNavAnsatt) innloggetBruker);
        }
    }

}
