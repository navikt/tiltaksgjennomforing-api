package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoering;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SystembrukerProperties;
import no.nav.tag.tiltaksgjennomforing.journalfoering.InternalAvtaleController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InternalAvtaleControllerTest {

    final UUID AVTALE_ID_1 = UUID.randomUUID();
    final UUID AVTALE_ID_2 = UUID.randomUUID();
    final UUID AVTALE_ID_3 = UUID.randomUUID();

    @InjectMocks
    private InternalAvtaleController internalAvtaleController;

    @Mock
    private InnloggingService innloggingService;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    SystembrukerProperties systembrukerProperties;

    @Test
    public void henterAvtalerTilJournalfoering() {
        Iterable<Avtale> alleAvtaler = godkjenteAvtaler();
        Avtale journalfoert = alleAvtaler.iterator().next();
        journalfoert.setJournalpostId("1");

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.findAll()).thenReturn(alleAvtaler);

        Iterable<AvtaleTilJournalfoering> avtalerTilJournalfoering = internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        avtalerTilJournalfoering.forEach(avtaleTilJournalfoering -> assertNotEquals(journalfoert.getId(), avtaleTilJournalfoering.getId()));
    }

    @Test
    public void ingenAvtaleTilJournalfoering() {
        Iterable<Avtale> alleAvtaler = godkjenteAvtaler();
        alleAvtaler.forEach(avtale -> avtale.setJournalpostId("DONE"));

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.findAll()).thenReturn(alleAvtaler);

        assertFalse(internalAvtaleController.hentIkkeJournalfoerteAvtaler().iterator().hasNext());
    }

    @Test(expected = TilgangskontrollException.class)
    public void henterIkkeAvtalerTilJournalfoering() {
        doThrow(TilgangskontrollException.class).when(innloggingService).validerSystembruker();

        internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        verify(avtaleRepository, never()).findAll();
    }

    @Test
    public void journalfoererAvtaler() {
        Map<UUID, String> map = new HashMap<>();
        map.put(AVTALE_ID_1, "journalId-1");
        map.put(AVTALE_ID_2, "journalId-2");
        map.put(AVTALE_ID_3, "journalId-3");

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.findAllById(map.keySet())).thenReturn(godkjenteAvtaler());
        internalAvtaleController.journalfoerAvtaler(map);
        verify(avtaleRepository, atLeastOnce()).saveAll(anyIterable());
    }

    @Test(expected = TilgangskontrollException.class)
    public void journalfoererIkkeAvtaler() {
        doThrow(TilgangskontrollException.class).when(innloggingService).validerSystembruker();
        internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        verify(avtaleRepository, never()).findAllById(anyIterable());
        verify(avtaleRepository, never()).saveAll(anyIterable());
    }


    private List<Avtale> godkjenteAvtaler() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setId(AVTALE_ID_1);
        Avtale avtale2 = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale2.setId(AVTALE_ID_2);
        Avtale avtale3 = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale3.setId(AVTALE_ID_3);
        return Arrays.asList(avtale, avtale2, avtale3);
    }


}
