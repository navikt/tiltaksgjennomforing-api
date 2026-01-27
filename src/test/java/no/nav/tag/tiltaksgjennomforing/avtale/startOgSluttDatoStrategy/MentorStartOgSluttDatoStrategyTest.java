package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MENTOR;

public class MentorStartOgSluttDatoStrategyTest {

    private Avtale avtale;

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), MENTOR), Avtaleopphav.VEILEDER, TestData.enNavIdent());
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void endreMentortilskudd__startdato_er_etter_sluttdato() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> endreAvtale(endreAvtale));
    }

    private void endreAvtale(EndreAvtale endreAvtale) {
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_6mnd_hvis_ikke_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(endreAvtale);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_6mnd_hvis_ikke_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND, () -> endreAvtale(endreAvtale));
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_6mnd_hvis_ikke_spesiellt() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND, () -> endreAvtale(endreAvtale));
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_36mnd_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();

        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(36).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        endreAvtale(endreAvtale);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_36mnd_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();

        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(36);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_36_MND, () -> endreAvtale(endreAvtale));
    }

    @Test
    public void For_gammel_for_og_ha_mentor_tilskudd(){
        Fnr deltakerFnr = Fnr.generer(1954,1,29);
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(32);
        boolean erAvtaleInngått = false;
        boolean erGodkjentForEtterregistrering = false;
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        MentorStartOgSluttDatoStrategy mentorStartOgSluttDatoStrategy = new MentorStartOgSluttDatoStrategy(avtale.getKvalifiseringsgruppe(), avtale.erOpprettetEllerEndretAvArena());
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> mentorStartOgSluttDatoStrategy.sjekkStartOgSluttDato(startDato, sluttDato ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }

    @Test
    public void Avtaler_med_sluttdato_tilbake_i_tiden_skal_ikke_sjekke_varighet() {
        Avtale avtale = TestData.enMentorArenaAvtaleMedAltUtfyltMedSluttDatoTilbakeITid();
        EndreAvtale endreAvtale = EndreAvtale.fraAvtale(avtale);
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("Onkel");
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
    }
}
