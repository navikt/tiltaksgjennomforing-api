package no.nav.tag.tiltaksgjennomforing.journalfoering;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoeringMapper.tilJournalfoering;
import static org.junit.Assert.*;

public class AvtaleTilJournalfoeringMapperTest {

    private Avtale avtale;
    private AvtaleInnhold avtaleInnhold;
    private AvtaleTilJournalfoering tilJournalfoering;
    private GodkjentPaVegneGrunn grunn;

    @Before
    public void setUp() {
        avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtaleInnhold = avtale.getVersjoner().get(0);
        grunn = new GodkjentPaVegneGrunn();
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
        avtale.setStillingstype(Stillingstype.FAST);

        tilJournalfoering = tilJournalfoering(avtaleInnhold);

        assertEquals(avtaleId.toString(), tilJournalfoering.getAvtaleId().toString());
        assertEquals(avtaleInnhold.getId().toString(), tilJournalfoering.getAvtaleVersjonId().toString());
        assertEquals(avtale.getDeltakerFnr().asString(), tilJournalfoering.getDeltakerFnr());
        assertEquals(avtale.getBedriftNr().asString(), tilJournalfoering.getBedriftNr());
        assertEquals(avtale.getVeilederNavIdent().asString(), tilJournalfoering.getVeilederNavIdent());
        assertEquals(avtale.getOpprettetTidspunkt().toLocalDate(), tilJournalfoering.getOpprettet());
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
        assertEquals(avtale.getStartDato(), tilJournalfoering.getStartDato());
        assertEquals(avtale.getSluttDato(), tilJournalfoering.getSluttDato());
        assertEquals(avtale.getStillingprosent(), tilJournalfoering.getStillingprosent());
        assertEquals(avtale.getGodkjentAvDeltaker().toLocalDate(), tilJournalfoering.getGodkjentAvDeltaker());
        assertEquals(avtale.getGodkjentAvArbeidsgiver().toLocalDate(), tilJournalfoering.getGodkjentAvArbeidsgiver());
        assertEquals(avtale.getGodkjentAvVeileder().toLocalDate(), tilJournalfoering.getGodkjentAvVeileder());
        assertEquals(avtale.isGodkjentPaVegneAv(), tilJournalfoering.isGodkjentPaVegneAv());
        assertEquals(avtale.getVersjon(), tilJournalfoering.getVersjon());
        assertEquals(avtale.getTiltakstype(), tilJournalfoering.getTiltakstype());
        assertEquals(avtale.getArbeidsgiverKontonummer(), tilJournalfoering.getArbeidsgiverKontonummer());
        assertEquals(avtale.getStillingstittel(), tilJournalfoering.getStillingstittel());
        assertEquals(avtale.getArbeidsoppgaver(), tilJournalfoering.getArbeidsoppgaver());
        assertEquals(avtale.getLonnstilskuddProsent(), tilJournalfoering.getLonnstilskuddProsent());
        assertEquals(avtale.getManedslonn(), tilJournalfoering.getManedslonn());
        assertEquals(avtale.getFeriepengesats(), tilJournalfoering.getFeriepengesats());
        assertEquals(avtale.getArbeidsgiveravgift(), tilJournalfoering.getArbeidsgiveravgift());
        assertNotNull(avtaleInnhold.getStillingstype());
        assertEquals(avtaleInnhold.getStillingstype(), tilJournalfoering.getStillingstype());
    }

    @Test
    public void paaVegneGrunnErIkkeBankId() {
        grunn.setIkkeBankId(true);
        avtale.setGodkjentPaVegneGrunn(grunn);
        tilJournalfoering = tilJournalfoering(avtaleInnhold);
        assertTrue(tilJournalfoering.getGodkjentPaVegneGrunn().isIkkeBankId());
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isDigitalKompetanse());
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isReservert());
    }

    @Test
    public void paaVegneGrunnErDigitalKompetanse() {
        grunn.setDigitalKompetanse(true);
        avtale.setGodkjentPaVegneGrunn(grunn);
        tilJournalfoering = tilJournalfoering(avtaleInnhold);
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isIkkeBankId());
        assertTrue(tilJournalfoering.getGodkjentPaVegneGrunn().isDigitalKompetanse());
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isReservert());
    }

    @Test
    public void paaVegneGrunnErReservert() {
        grunn.setReservert(true);
        avtale.setGodkjentPaVegneGrunn(grunn);
        tilJournalfoering = tilJournalfoering(avtaleInnhold);
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isIkkeBankId());
        assertFalse(tilJournalfoering.getGodkjentPaVegneGrunn().isDigitalKompetanse());
        assertTrue(tilJournalfoering.getGodkjentPaVegneGrunn().isReservert());

        avtale.setGodkjentPaVegneGrunn(null);
        tilJournalfoering = tilJournalfoering(avtaleInnhold);
        assertNull(tilJournalfoering.getGodkjentPaVegneGrunn());
    }

    @Test
    public void ingenPaaVegneGrunn() {
        avtale.setGodkjentPaVegneGrunn(null);
        tilJournalfoering = tilJournalfoering(avtaleInnhold);
        assertNull(tilJournalfoering.getGodkjentPaVegneGrunn());
    }

    @Test
    public void mapperMaal() {
        Maal maal = new Maal();
        maal.setKategori(MaalKategori.FÅ_JOBB_I_BEDRIFTEN);
        maal.setBeskrivelse("Beskrivelse");

        Maal maal2 = new Maal();
        maal2.setKategori(MaalKategori.UTPRØVING);
        maal2.setBeskrivelse("Beskrivelse-2");

        avtaleInnhold.setMaal(Arrays.asList(maal, maal2));

        tilJournalfoering = tilJournalfoering(avtaleInnhold);

        tilJournalfoering.getMaal().forEach(maalet -> {
            if (maalet.getKategori().equals(MaalKategori.FÅ_JOBB_I_BEDRIFTEN.getVerdi())) {
                assertEquals("Beskrivelse", maalet.getBeskrivelse());
            } else if (maalet.getKategori().equals(MaalKategori.UTPRØVING.getVerdi())) {
                assertEquals("Beskrivelse-2", maalet.getBeskrivelse());
            } else {
                fail("Mål; " + maalet);
            }
        });
    }

}
