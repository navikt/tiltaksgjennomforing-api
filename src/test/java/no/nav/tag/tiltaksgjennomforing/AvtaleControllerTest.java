package no.nav.tag.tiltaksgjennomforing;

import lombok.ToString;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId()).getBody();

        assertThat(hentetAvtale).isEqualTo(avtale);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void hentSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.hent(avtale.getId());
    }

    @Test
    public void hentSkalReturnereForbiddenHvisInnloggetBrukerIkkeHarTilgang() {
        vaerInnloggetSom(new Veileder("Z909090"));
        Avtale avtale = TestData.minimalAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hent(avtale.getId());
        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));

        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getArbeidsgiverFnr()));

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(svar.getHeaders().getLocation().getPath()).isEqualTo("/avtaler/" + avtale.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErVeileder() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Veileder(avtale.getVeilederNavIdent()));
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void endreAvtaleSkalReturnereForbiddenHvisInnloggetPersonIkkeHarTilgang() {
        Avtale avtale = TestData.minimalAvtale();
        vaerInnloggetSom(new Bruker("89898989898"));

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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

        hentedeAvtaler.forEach(avtale -> assertThat(avtale.erTilgjengeligFor(innloggetBruker)).isTrue());
        assertThat(hentedeAvtaler.size()).isEqualTo(avtalerBrukerHarTilgangTil.size());
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
    public void hentRolleSkalKasteResourceNotFoundExceptionHvisAvtaleIkkeFins() {
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

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void hentRolleSkalReturnereOkMedEnRolleHvisInnloggetBrukerErTilknyttetAvtale() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltaker = new Bruker(avtale.getDeltakerFnr());
        vaerInnloggetSom(deltaker);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.hentRolle(avtale.getId());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(svar.getBody()).isEqualTo(Rolle.DELTAKER);
    }

    @Test
    public void endreAvtaleSkalReturnereForbiddenlHvisInnloggetPersonErDeltaker() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltaker = new Bruker(avtale.getDeltakerFnr());
        vaerInnloggetSom(deltaker);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void endreAvtaleSkalReturnereOkHvisInnloggetPersonErArbeidsgiver() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker arbeidsgiver = new Bruker(avtale.getArbeidsgiverFnr());
        vaerInnloggetSom(arbeidsgiver);

        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertThat(svar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
