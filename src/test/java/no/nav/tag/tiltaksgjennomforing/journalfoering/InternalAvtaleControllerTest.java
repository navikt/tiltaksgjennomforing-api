package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class InternalAvtaleControllerTest {

    private static final UUID AVTALE_ID_1 = UUID.randomUUID();
    private static final UUID AVTALE_ID_2 = UUID.randomUUID();
    private static final UUID AVTALE_ID_3 = UUID.randomUUID();
    private List<Avtale> avtaleList = avtalerMedFemGodkjenteVersjoner();
    private List<UUID> avtaleIds = Arrays.asList(AVTALE_ID_1, AVTALE_ID_2, AVTALE_ID_3);

    @InjectMocks
    private InternalAvtaleController internalAvtaleController;

    @Mock
    private InnloggingService innloggingService;

    @Mock
    private AvtaleRepository avtaleRepository;

    @Mock
    private AvtaleInnholdRepository avtaleInnholdRepository;

    @Test
    public void henterAvtalerTilJournalfoering() {
        //Setter en versjon til journalført
        Avtale avtaleMedEnJournalfortVersjon = avtaleList.get(1);
        AvtaleInnhold journalfort = avtaleMedEnJournalfortVersjon.getVersjoner().get(0);
        journalfort.setJournalpostId("1");

        doNothing().when(innloggingService).validerSystembruker();
       // when(avtaleInnholdRepository.finnAvtaleIdTilJournalfoering()).thenReturn(avtaleIds);
        when(avtaleRepository.findAllById(eq(avtaleIds))).thenReturn(avtaleList);
        List<AvtaleTilJournalfoering> avtalerTilJournalfoering = internalAvtaleController.hentIkkeJournalfoerteAvtaler();

        assertEquals(4, avtalerTilJournalfoering.size());
        avtalerTilJournalfoering.forEach(avtaleTilJournalfoering -> assertNotEquals(journalfort.getId(), avtaleTilJournalfoering.getAvtaleVersjonId()));
    }

    @Test
    public void ingenAvtaleTilJournalfoering() {
        doNothing().when(innloggingService).validerSystembruker();
        avtaleList.stream().flatMap(avtale -> avtale.getVersjoner().stream()).forEach(avtale -> avtale.setJournalpostId("1"));
//        when(avtaleInnholdRepository.finnAvtaleIdTilJournalfoering()).thenReturn(avtaleIds);
        when(avtaleRepository.findAllById(avtaleIds)).thenReturn(avtaleList);

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
        List<AvtaleInnhold> godkjenteAvtaleVersjoner = avtalerMedFemGodkjenteVersjoner().stream().flatMap(avtale ->avtale.getVersjoner().stream()).collect(Collectors.toList());
        Map<UUID, String> map = new HashMap<>();
        godkjenteAvtaleVersjoner.forEach(avtaleInnhold -> map.put(avtaleInnhold.getId(), "1"));

        doNothing().when(innloggingService).validerSystembruker();
        when(avtaleInnholdRepository.findAllById(map.keySet())).thenReturn(godkjenteAvtaleVersjoner);
        internalAvtaleController.journalfoerAvtaler(map);
        verify(avtaleInnholdRepository, atLeastOnce()).saveAll(anyIterable());
    }

    @Test(expected = TilgangskontrollException.class)
    public void journalfoererIkkeAvtaler() {
        doThrow(TilgangskontrollException.class).when(innloggingService).validerSystembruker();
        internalAvtaleController.hentIkkeJournalfoerteAvtaler();
        verify(avtaleRepository, never()).findAllById(anyIterable());
        verify(avtaleRepository, never()).saveAll(anyIterable());
    }

    private static List<Avtale> avtalerMedFemGodkjenteVersjoner() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setId(AVTALE_ID_1);
        Avtale avtale2 = TestData.enAvtaleMedFlereVersjoner();
        avtale2.getVersjoner().get(0).setGodkjentAvVeileder(LocalDateTime.now());
        avtale2.setId(AVTALE_ID_2);
        Avtale avtale3 = TestData.enAvtaleMedFlereVersjoner();
        avtale3.getVersjoner().get(0).setGodkjentAvVeileder(LocalDateTime.now());
        avtale3.setId(AVTALE_ID_3);
        return Arrays.asList(avtale, avtale2, avtale3);
    }
}

