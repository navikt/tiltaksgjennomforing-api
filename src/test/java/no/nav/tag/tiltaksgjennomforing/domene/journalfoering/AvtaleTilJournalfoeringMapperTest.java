package no.nav.tag.tiltaksgjennomforing.domene.journalfoering;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

public class AvtaleTilJournalfoeringMapperTest {

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AvtaleTilJournalfoering.DATE_PATTERN);

    private Avtale avtale;
    private AvtaleTilJournalfoering tilJournalfoering;

    @Before
    public void setUp() {
        avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
    }

    @After
    public void tearDown() {
        avtale = null;
        tilJournalfoering = null;
    }

    @Test
    public void mapper() {
        final UUID avtaleId = UUID.randomUUID();
        avtale.setId(avtaleId);
        avtale.setGodkjentPaVegneAv(true);
        avtale.setOpprettetTidspunkt(LocalDateTime.now());

        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);

        assertEquals(avtaleId.toString(), tilJournalfoering.getId().toString());
        assertEquals(avtale.getDeltakerFnr().getFnr(), tilJournalfoering.getDeltakerFnr());
        assertEquals(avtale.getBedriftNr().getBedriftNr(), tilJournalfoering.getBedriftNr());
        assertEquals(avtale.getVeilederNavIdent().getId(), tilJournalfoering.getVeilederNavIdent());
        assertEquals(avtale.getOpprettetTidspunkt().toLocalDate().format(formatter), tilJournalfoering.getOpprettet());
        assertEquals(avtale.getVersjon(), tilJournalfoering.getVersjon());
        assertEquals(avtale.getDeltakerFornavn(), tilJournalfoering.getDeltakerFornavn());
        assertEquals(avtale.getDeltakerEtternavn(), tilJournalfoering.getDeltakerEtternavn());
        assertEquals(avtale.getDeltakerTlf(), tilJournalfoering.getDeltakerTlf());
        assertEquals(avtale.getBedriftNavn(), tilJournalfoering.getBedriftNavn());
        assertEquals(avtale.getArbeidsgiverFornavn(), tilJournalfoering.getArbeidsgiverFornavn());
        assertEquals(avtale.getArbeidsgiverEtternavn(), tilJournalfoering.getArbeidsgiverEtternavn());
        assertEquals(avtale.getArbeidsgiverTlf(), tilJournalfoering.getArbeidsgiverTlf());
        assertEquals(avtale.getVeilederFornavn(), tilJournalfoering.getVeilederFornavn());
        assertEquals(avtale.getVeilederEtternavn(), tilJournalfoering.getVeilederEtternavn());
        assertEquals(avtale.getVeilederTlf(), tilJournalfoering.getVeilederTlf());
        assertEquals(avtale.getOppfolging(), tilJournalfoering.getOppfolging());
        assertEquals(avtale.getTilrettelegging(), tilJournalfoering.getTilrettelegging());
        assertEquals(avtale.getStartDato().format(formatter), tilJournalfoering.getStartDato());
        assertEquals(avtale.getArbeidstreningLengde(), tilJournalfoering.getArbeidstreningLengde());
        assertEquals(avtale.getArbeidstreningStillingprosent(), tilJournalfoering.getArbeidstreningStillingprosent());
        assertEquals(avtale.getGodkjentAvDeltaker().toLocalDate().format(formatter), tilJournalfoering.getGodkjentAvDeltaker());
        assertEquals(avtale.getGodkjentAvArbeidsgiver().toLocalDate().format(formatter), tilJournalfoering.getGodkjentAvArbeidsgiver());
        assertEquals(avtale.getGodkjentAvVeileder().toLocalDate().format(formatter), tilJournalfoering.getGodkjentAvVeileder());
        assertEquals(Boolean.toString(avtale.isGodkjentPaVegneAv()), tilJournalfoering.getGodkjentPaVegneAv());

    }

    @Test
    public void mapperAllePaaVegneGrunner() {
        GodkjentPaVegneGrunn grunn = new GodkjentPaVegneGrunn();
        grunn.setIkkeBankId(true);

        avtale.setGodkjentPaVegneGrunn(grunn);

        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);
        assertEquals(AvtaleTilJournalfoering.IKKE_BANKID, tilJournalfoering.getGodkjentPaVegneGrunn());

        grunn = new GodkjentPaVegneGrunn();
        grunn.setDigitalKompetanse(true);
        avtale.setGodkjentPaVegneGrunn(grunn);
        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);
        assertEquals(AvtaleTilJournalfoering.DIGITAL_KOMPETANSE, tilJournalfoering.getGodkjentPaVegneGrunn());

        grunn = new GodkjentPaVegneGrunn();
        grunn.setReservert(true);
        avtale.setGodkjentPaVegneGrunn(grunn);
        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);
        assertEquals(AvtaleTilJournalfoering.RESERVERT, tilJournalfoering.getGodkjentPaVegneGrunn());

        avtale.setGodkjentPaVegneGrunn(null);
        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);
        assertNull(tilJournalfoering.getGodkjentPaVegneGrunn());
    }

    @Test
    public void mapperOppgaver() {
        Oppgave oppgave = new Oppgave();
        oppgave.setTittel("Tittel");
        oppgave.setOpplaering("Opplæring");
        oppgave.setBeskrivelse("Beskrivelse");

        Oppgave oppgave2 = new Oppgave();
        oppgave2.setTittel("Tittel-2");
        oppgave2.setOpplaering("Opplæring-2");
        oppgave2.setBeskrivelse("Beskrivelse-2");
        avtale.setOppgaver(Arrays.asList(oppgave, oppgave2));

        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);

        tilJournalfoering.getOppgaver().forEach(oppg -> {
            if (oppg.getTittel().equals("Tittel")) {
                assertTrue(oppg.getBeskrivelse().equals("Beskrivelse") && oppg.getOpplaering().equals("Opplæring"));
            } else if (oppg.getTittel().equals("Tittel-2")) {
                assertTrue(oppg.getBeskrivelse().equals("Beskrivelse-2") && oppg.getOpplaering().equals("Opplæring-2"));
            } else {
                fail("Oppgave; " + oppg);
            }
        });
    }

    @Test
    public void mapperMaal() {
        Maal maal = new Maal();
        maal.setKategori("Kategori");
        maal.setBeskrivelse("Beskrivelse");

        Maal maal2 = new Maal();
        maal2.setKategori("Kategori-2");
        maal2.setBeskrivelse("Beskrivelse-2");

        avtale.setMaal(Arrays.asList(maal, maal2));
        tilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale);

        tilJournalfoering.getMaal().forEach(maalet -> {
            if (maalet.getKategori().equals("Kategori"))  {
                assertEquals("Beskrivelse", maalet.getBeskrivelse());
            } else if(maalet.getKategori().equals("Kategori-2")){
                assertEquals("Beskrivelse-2", maalet.getBeskrivelse());
            } else {
                fail("Mål; " + maalet);
            }
        });
    }

}
