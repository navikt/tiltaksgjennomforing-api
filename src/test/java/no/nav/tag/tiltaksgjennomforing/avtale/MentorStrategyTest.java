package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MENTOR;

public class MentorStrategyTest {

    private Avtale avtale;

    @BeforeEach
    public void setUp() {
        avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), MENTOR), TestData.enNavIdent());
    }

    @Test
    public void endreMentortilskudd__startdato_er_etter_sluttdato() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> endreAvtale(endreAvtale));
    }

    private void endreAvtale(EndreAvtale endreAvtale) {
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.noneOf(Tiltakstype.class));
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_36mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(36);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(endreAvtale);
    }

    @Test
    public void endreMentortilskudd__startdato_og_sluttdato_satt_over_36mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = LocalDate.now();
        LocalDate sluttDato = startDato.plusMonths(36).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MENTOR, () -> endreAvtale(endreAvtale));
    }
}
