package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.controller.AvtaleController;
import no.nav.tag.tiltaksgjennomforing.controller.ResourceNotFoundException;
import no.nav.tag.tiltaksgjennomforing.controller.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.controller.TilgangskontrollUtils;
import no.nav.tag.tiltaksgjennomforing.domene.*;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvtaleControllerTest {

    @InjectMocks
    private AvtaleController avtaleController;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    private TilgangskontrollUtils tilgangskontroll;

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = TestData.lagAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId()).getBody();

        assertEquals(avtale, hentetAvtale);
    }

    @Test
    public void hentSkalReturnereForbiddenHvisInnloggetBrukerIkkeHarTilgang() {
        vaerInnloggetSom(new Veileder("Z909090"));
        Avtale avtale = TestData.minimalAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hent(avtale.getId());
        assertEquals(svar.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));

        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getArbeidsgiverFnr()));

        // TODO: Feil rekkefølge på expected og actual
        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId());
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        // TODO: Endre til å kaste ResourceNotFoundException
        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertEquals(svar.getStatusCodeValue(), 200);
    }

    @Test
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetBrukerIkkeHarTilgang() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Bruker("89898989898"));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertEquals(svar.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void hentAlleAvtalerInnloggetBrukerHarTilgangTilSkalIkkeReturnereAvtalerManIkkeHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.minimalAvtale();
        Avtale avtaleUtenTilgang = Avtale.nyAvtale(new OpprettAvtale(new Fnr("89898989898"), new Fnr("89898989898")), new NavIdent("X643564"));

        Bruker innloggetBruker = new Bruker(avtaleMedTilgang.getDeltakerFnr());
        vaerInnloggetSom(innloggetBruker);

        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));

        when(avtaleRepository.findAll()).thenReturn(alleAvtaler);

        List<Avtale> hentedeAvtaler = new ArrayList<>();
        for (Avtale avtale : avtaleController.hentAlleAvtalerInnloggetBrukerHarTilgangTil()) {
            hentedeAvtaler.add(avtale);
        }
        hentedeAvtaler.forEach(avtale -> assertTrue(avtale.erTilgjengeligFor(innloggetBruker)));
        assertEquals(avtalerBrukerHarTilgangTil.size(), hentedeAvtaler.size());
    }

    private List<Avtale> lagListeMedAvtaler(Avtale avtale, int antall) {
        List<Avtale> avtaler = new ArrayList<>();
        for (int i=0; i<=antall; i++) {
            avtaler.add(avtale);
        }
        return avtaler;
    }

    private void vaerInnloggetSom(Person person) {
        when(tilgangskontroll.hentInnloggetPerson()).thenReturn(person);
        if (person instanceof Veileder) {
            when(tilgangskontroll.hentInnloggetVeileder()).thenReturn((Veileder) person);
        }
    }

    @Test(expected = ResourceNotFoundException.class)
    public void hentRolleSkalReturnereNotFoundHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.minimalAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hentRolle(avtale.getId());
    }

    @Test
    public void hentRolleSkalReturnereForbiddenHvisIkkeTilknyttetAvtale() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltakerUtenTilgang = new Bruker("00000000000");
        vaerInnloggetSom(deltakerUtenTilgang);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hentRolle(avtale.getId());

        assertEquals(HttpStatus.FORBIDDEN, svar.getStatusCode());
    }

    @Test
    public void hentRolleSkalReturnereOkMedEnRolleHvisInnloggetBrukerErTilknyttetAvtale() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltaker = new Bruker(avtale.getDeltakerFnr());
        vaerInnloggetSom(deltaker);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hentRolle(avtale.getId());

        assertEquals(HttpStatus.OK, svar.getStatusCode());
        assertEquals(Rolle.DELTAKER, svar.getBody());
    }
}
