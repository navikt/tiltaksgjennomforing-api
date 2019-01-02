package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.controller.AvtaleController;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.OpprettAvtale;
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
        Avtale hentetAvtale = avtaleController.hent(avtale.getId()).getBody();

        assertEquals(avtale, hentetAvtale);
    }

    @Test
    public void opprettAvtaleSkalReturnereCreatedOgOpprettetLokasjon() {
        Avtale avtale = TestData.minimalAvtale();

        when(avtaleRepository.save(any(Avtale.class))).thenReturn(avtale);
        ResponseEntity svar = avtaleController.opprettAvtale(new OpprettAvtale(avtale.getDeltakerFnr(), avtale.getVeilederNavIdent()));

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId());
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.empty());
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = TestData.lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        when(avtaleRepository.save(avtale)).thenReturn(avtale);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale.getVersjon(), TestData.ingenEndring());

        assertEquals(svar.getStatusCodeValue(), 200);
    }
}
