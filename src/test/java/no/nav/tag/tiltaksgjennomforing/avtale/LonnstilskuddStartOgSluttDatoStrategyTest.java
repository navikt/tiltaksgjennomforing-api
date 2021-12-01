package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VARIG_LONNSTILSKUDD;

class LonnstilskuddStartOgSluttDatoStrategyTest {

    private static Avtale enMidlertidig() {
        return Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent());
    }

    private static Avtale enVarig() {
        return Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), VARIG_LONNSTILSKUDD), TestData.enNavIdent());
    }

    private void endreAvtale(Avtale avtale, EndreAvtale endreAvtale) {
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.noneOf(Tiltakstype.class));
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(enMidlertidig(), endreAvtale);
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD, () -> endreAvtale(enMidlertidig(), endreAvtale));
    }

    @Test
    public void endreVarigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(enVarig(), endreAvtale);
    }
}