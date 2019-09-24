package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SystembrukerProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoering;
import no.nav.tag.tiltaksgjennomforing.journalfoering.InternalAvtaleController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        List<Avtale> avtaleList = StreamSupport.stream(godkjenteAvtaler().spliterator(), false).collect(Collectors.toList());

        Avtale ikkeJournalfoert1 = avtaleList.get(0);
        Avtale ikkeJournalfoert2 = avtaleList.get(1);
        Avtale journalfoert = avtaleList.get(2);
        journalfoert.setJournalpostId("1");

        UUID idIkkeJournalfoert1 = ikkeJournalfoert1.getId();
        UUID idIkkeJournalfoert2 = ikkeJournalfoert2.getId();
        List<UUID> idIkkeJournalfoertList = Arrays.asList(idIkkeJournalfoert1, idIkkeJournalfoert2);

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.finnAvtaleIdTilJournalfoering()).thenReturn(idIkkeJournalfoertList);
        when(avtaleRepository.findAllById(idIkkeJournalfoertList)).thenReturn(Arrays.asList(ikkeJournalfoert1, ikkeJournalfoert2));

        Iterable<AvtaleTilJournalfoering> avtalerTilJournalfoering = internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        avtalerTilJournalfoering.forEach(avtaleTilJournalfoering -> assertNotEquals(journalfoert.getId(), avtaleTilJournalfoering.getId()));
    }

    @Test
    public void ingenAvtaleTilJournalfoering() {
        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleRepository.finnAvtaleIdTilJournalfoering()).thenReturn(Arrays.asList());

        internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        verify(avtaleRepository, never()).findAllById(anyIterable());
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
