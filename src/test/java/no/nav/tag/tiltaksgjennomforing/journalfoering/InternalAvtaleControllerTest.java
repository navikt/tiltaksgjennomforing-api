package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InternalAvtaleControllerTest {

    private static final UUID AVTALE_ID_1 = UUID.randomUUID();
    private static final UUID AVTALE_ID_2 = UUID.randomUUID();
    private static final UUID AVTALE_ID_3 = UUID.randomUUID();

    @InjectMocks
    private InternalAvtaleController internalAvtaleController;

    @Mock
    private InnloggingService innloggingService;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Test
    public void henterAvtalerTilJournalfoering() {
        List<Avtale> avtaleList = godkjenteAvtaler();

        Avtale journalfoert = avtaleList.get(2);
        journalfoert.setJournalpostId("1");

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.findAll()).thenReturn(avtaleList);

        List<AvtaleTilJournalfoering> avtalerTilJournalfoering = internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        avtalerTilJournalfoering.forEach(avtaleTilJournalfoering -> assertNotEquals(journalfoert.getId(), avtaleTilJournalfoering.getId()));
    }

    @Test
    public void ingenAvtaleTilJournalfoering() {
        doNothing().when(innloggingService).validerSystembruker();
        List<Avtale> avtaler = godkjenteAvtaler();
        avtaler.forEach(avtale -> avtale.setJournalpostId("1"));
        when(avtaleRepository.findAll()).thenReturn(avtaler);
        assertTrue(internalAvtaleController.hentIkkeJournalfoerteAvtaler().isEmpty());
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

    private static List<Avtale> godkjenteAvtaler() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setId(AVTALE_ID_1);
        Avtale avtale2 = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale2.setId(AVTALE_ID_2);
        Avtale avtale3 = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale3.setId(AVTALE_ID_3);
        return Arrays.asList(avtale, avtale2, avtale3);
    }
}
