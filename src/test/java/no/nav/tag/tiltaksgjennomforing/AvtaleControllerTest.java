package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvtaleControllerTest {

    @InjectMocks
    private AvtaleController avtaleController;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId());

        assertEquals(avtale, hentetAvtale);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Fnr deltakerFnr = new Fnr("12345678012");
        Avtale avtale = Avtale.nyAvtale(deltakerFnr);

        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        ResponseEntity svar = avtaleController.opprettAvtale(deltakerFnr);

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId());
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 200);
    }

    @Test
    public void endreMaalSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreMaal(avtale.getId(), avtale.getMaal());

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreMaalSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreMaal(avtale.getId(), avtale.getMaal());

        assertEquals(svar.getStatusCodeValue(), 200);
    }

    @Test
    public void endreOppgaveSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreOppgaver(avtale.getId(), avtale.getOppgaver());

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreOppgaveSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreOppgaver(avtale.getId(), avtale.getOppgaver());

        assertEquals(svar.getStatusCodeValue(), 200);
    }
}
