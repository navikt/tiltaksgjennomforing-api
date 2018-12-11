package no.nav.tag.tiltaksgjennomforing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AvtaleControllerTest {

    private AvtaleController avtaleController;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    private OppgaveRepository oppgaveRepository;

    @Mock
    private MaalRepository maalRepository;

    @Before
    public void setUp() {
        initMocks(this);
        avtaleController = new AvtaleController(avtaleRepository, oppgaveRepository, maalRepository);
    }

    @Test
    public void hentSkalReturnereRiktigAvtale() {
        Avtale avtale = lagAvtale();
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        Avtale hentetAvtale = avtaleController.hent(avtale.getId());

        assertEquals(lagAvtale(), hentetAvtale);
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
        Avtale avtale = lagAvtale();
        Maal maal = lagMaal();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(maalRepository.save(maal)).thenReturn(maal);
        ResponseEntity svar = avtaleController.opprettMaal(avtale.getId(), maal);

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId() + "/maal/" + maal.getId());
    }

    @Test
    public void opprettMaalSkalReturnereNotFoundHvisAvtaleIkkeFins() {
        Avtale avtale = lagAvtale();
        Maal maal = lagMaal();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.opprettMaal(avtale.getId(), maal);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void opprettOppgaveSkalReturnereCreatedOgLokasjonHvisMaalBlirOpprettet() {
        Avtale avtale = lagAvtale();
        Oppgave oppgave = lagOppgave();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(oppgaveRepository.save(oppgave)).thenReturn(oppgave);
        ResponseEntity svar = avtaleController.opprettOppgave(avtale.getId(), oppgave);

        assertEquals(svar.getStatusCodeValue(), 201);
        assertEquals(svar.getHeaders().getLocation().getPath(), "/avtaler/" + avtale.getId() + "/oppgaver/" + oppgave.getId());
    }

    @Test
    public void opprettOppgaveSkalReturnereNotFoundHvisAvtaleIkkeFins() {
        Avtale avtale = lagAvtale();
        Oppgave oppgave = lagOppgave();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.opprettOppgave(avtale.getId(), oppgave);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereNotFoundHvisDenIkkeFins() {
        Avtale avtale = lagAvtale();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(false);
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 404);
    }

    @Test
    public void endreAvtaleSkalReturnereOkEtterEndretAvtale() {
        Avtale avtale = lagAvtale();
        when(avtaleRepository.existsById(avtale.getId())).thenReturn(true);
        when(avtaleRepository.findById(avtale.getId())).thenReturn(Optional.of(avtale));
        ResponseEntity svar = avtaleController.endreAvtale(avtale.getId(), avtale);

        assertEquals(svar.getStatusCodeValue(), 200);
    }

    private Avtale lagAvtale() {
        Avtale avtale = new Avtale();
        avtale.setId(1);
        avtale.setOpprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1));
        avtale.setDeltakerFornavn("Donald");
        avtale.setDeltakerEtternavn("Duck");
        avtale.setOppgaver(Collections.emptyList());
        avtale.setMaal(Collections.emptyList());
        return avtale;
    }

    private Maal lagMaal() {
        Maal maal = new Maal();
        maal.setId(1);
        maal.setOpprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1));
        maal.setAvtale(1);
        maal.setBeskrivelse("Beksrivelse");
        maal.setKategori("Kategori");
        return maal;
    }

    private Oppgave lagOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.setId(1);
        oppgave.setOpprettetTidspunkt(LocalDateTime.of(1, 1, 1, 1, 1));
        oppgave.setTittel("Tittel");
        oppgave.setBeskrivelse("Beksrivelse");
        oppgave.setOpplaering("Opplæring");
        oppgave.setAvtale(1);
        return oppgave;
    }
}
