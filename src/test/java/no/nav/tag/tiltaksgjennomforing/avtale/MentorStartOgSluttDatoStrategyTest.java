package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MENTOR;

public class MentorStartOgSluttDatoStrategyTest {

    private Avtale avtale;

    @BeforeEach
    public void setUp() {
        avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), MENTOR), TestData.enNavIdent());
    }

    @Test
    public void endreMentortilskudd__startdato_er_etter_sluttdato() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> endreAvtale(endreAvtale));
    }

    private void endreAvtale(EndreAvtale endreAvtale) {
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.noneOf(Tiltakstype.class), List.of());
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_6mnd_hvis_ikke_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(endreAvtale);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_6mnd_hvis_ikke_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND, () -> endreAvtale(endreAvtale));
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_6mnd_hvis_ikke_spesiellt() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(6);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_6_MND, () -> endreAvtale(endreAvtale));
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_36mnd_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();

        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(36).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        endreAvtale(endreAvtale);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_36mnd_spesiellt_tilpasset() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();

        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(36);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR_36_MND, () -> endreAvtale(endreAvtale));
    }
}
