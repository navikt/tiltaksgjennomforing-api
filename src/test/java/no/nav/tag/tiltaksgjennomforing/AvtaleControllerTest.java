package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.controller.AvtaleController;
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
        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr()));

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId());
    }

    @Test
    public void opprettAvtaleSkalReturnereForbiddenHvisInnloggetBrukerIkkeHarTilgang() {
        OpprettAvtale opprettAvtale = TestData.lagOpprettAvtale();
        vaerInnloggetSom(new Bruker(opprettAvtale.getDeltakerFnr()));
        ResponseEntity svar = avtaleController.opprettAvtale(opprettAvtale);
        assertEquals(svar.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

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
    public void hentAlleSkalBareReturnereAvtalerBrukerenHarTilgangTil() {
        Avtale avtaleMedTilgang = TestData.minimalAvtale();
        Avtale avtaleUtenTilgang = Avtale.nyAvtale(new OpprettAvtale(new Fnr("89898989898")), avtaleMedTilgang.getVeilederNavIdent());

        Bruker innloggetBruker = new Bruker(avtaleMedTilgang.getDeltakerFnr());
        vaerInnloggetSom(innloggetBruker);

        List<Avtale> avtalerBrukerHarTilgangTil = lagListeMedAvtaler(avtaleMedTilgang, 5);
        List<Avtale> alleAvtaler = new ArrayList<>();
        alleAvtaler.addAll(avtalerBrukerHarTilgangTil);
        alleAvtaler.addAll(lagListeMedAvtaler(avtaleUtenTilgang, 4));

        when(avtaleRepository.findAll()).thenReturn(alleAvtaler);

        List<Avtale> hentedeAvtaler = new ArrayList<>();
        for (Avtale avtale : avtaleController.hentAlle()) {
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
        when(tilgangskontroll.hentInnloggetBruker()).thenReturn(person);
    }
}
