package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.avtale;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatus;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatusReopsitory;
import no.nav.tag.tiltaksgjennomforing.domene.prosess.StatusJournalforing;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.prosess.JournalforingStatus.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JournalfoeringServiceTest {

    @Mock
    private GodkjentAvtaleProducer godkjentAvtaleProducer;

    @Mock
    private JournalforingStatusReopsitory journalForingStatusReopsitory;

    @InjectMocks
    private JournalfoeringService journalfoeringService;

    final UUID AVTALE_ID = UUID.randomUUID();
    private Avtale avtale;
    private StatusJournalforing statusJournalforing;

    @Before
    public void setUp(){
        avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
    }

    @Test
    public void senderAvtaleTilJournalfoering(){
        avtale.setId(AVTALE_ID);
        statusJournalforing = new StatusJournalforing(avtale.getId(), TIL_PROSESS);
        statusJournalforing.settIdOgOpprettetTidspunkt();

        journalfoeringService.sendTilJournalfoeringHvisGodkjentAvAlle(avtale);
        verify(godkjentAvtaleProducer, atLeastOnce()).sendAvtaleTilJournalfoering(eq(AVTALE_ID.toString()), anyString());
        verify(journalForingStatusReopsitory, atLeastOnce()).save(argThat(status ->  status.getStatus().equals(TIL_PROSESS) ));
    }

    @Test
    public void senderIkkeAvtaleTilJournalfoering(){
        avtale.setGodkjentAvVeileder(null);
        journalfoeringService.sendTilJournalfoeringHvisGodkjentAvAlle(avtale);
        verify(godkjentAvtaleProducer, never()).sendAvtaleTilJournalfoering(anyString(), anyString());
        verify(journalForingStatusReopsitory, never()).save(any(StatusJournalforing.class));
    }
}
