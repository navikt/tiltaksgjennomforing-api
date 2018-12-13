package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

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
    private OppgaveRepository oppgaveRepository;

    @Mock
    private MaalRepository maalRepository;

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId());

        assertEquals(TestData.lagAvtale(), hentetAvtale);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = lagAvtale();
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.opprettAvtale(avtale);

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId());
    }

    @Test
    public void opprettMaalSkalReturnereCreatedOgLokasjonHvisMaalBlirOpprettet() {
        Avtale avtale = TestData.lagAvtale();
        Maal maal = TestData.lagMaal();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(maalRepository.save(maal)).thenReturn(maal);
        ResponseEntity svar = avtaleController.opprettMaal(avtale.getId(), maal);
        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId() + "/maal/" + maal.getId());
    }

    @Test
    public void opprettMaalSkalReturnereNotFoundHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        Maal maal = TestData.lagMaal();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.opprettMaal(avtale.getId(), maal);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void opprettOppgaveSkalReturnereCreatedOgLokasjonHvisMaalBlirOpprettet() {
        Avtale avtale = TestData.lagAvtale();
        Oppgave oppgave = TestData.lagOppgave();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(oppgaveRepository.save(oppgave)).thenReturn(oppgave);
        ResponseEntity svar = avtaleController.opprettOppgave(avtale.getId(), oppgave);

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId() + "/oppgaver/" + oppgave.getId());
    }

    @Test
    public void opprettOppgaveSkalReturnereNotFoundHvisAvtaleIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        Oppgave oppgave = TestData.lagOppgave();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.opprettOppgave(avtale.getId(), oppgave);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 200);
    }
}
