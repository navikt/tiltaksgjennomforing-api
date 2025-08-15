package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VARIG_LONNSTILSKUDD;

class LonnstilskuddStartOgSluttDatoStrategyTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private static Avtale enMidlertidig() {
        return Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
    }

    private static Avtale enVarig() {
        return Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), VARIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
    }

    private void endreAvtale(Avtale avtale, EndreAvtale endreAvtale) {
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        Avtale avtale = enMidlertidig();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        endreAvtale(avtale, endreAvtale);
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd__SPESIELT_TILPASSET_INNSATS() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        Avtale avtale = enMidlertidig();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_24_MND, () -> endreAvtale(avtale, endreAvtale));
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd__VARIG_TILPASSET_INNSATS() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        Avtale avtale = enMidlertidig();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_24_MND, () -> endreAvtale(avtale, endreAvtale));
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd__SITUASJONSBESTEMT_INNSATS() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(12).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        Avtale avtale = enMidlertidig();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_12_MND, () -> endreAvtale(avtale, endreAvtale));
    }

    @Test
    public void endreMidlertidigLønnstilskudd__startdato_og_sluttdato_satt_over_12mnd__ikke_satt() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(12).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        Avtale avtale = enMidlertidig();
        avtale.setKvalifiseringsgruppe(null);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_12_MND, () -> endreAvtale(avtale, endreAvtale));
    }

    @Test
    public void endreVarigLønnstilskudd__startdato_og_sluttdato_satt_over_24mnd() {
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(24).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale(enVarig(), endreAvtale);
    }

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_MidlertidigLønnstilskudd() {
        Fnr deltakerFnr = new Fnr("12085220754");
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        Kvalifiseringsgruppe kvalifiseringsgruppe = Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS;
        MidlertidigLonnstilskuddStartOgSluttDatoStrategy midlertidigLonnstilskuddStartOgSluttDatoStrategy = new MidlertidigLonnstilskuddStartOgSluttDatoStrategy(kvalifiseringsgruppe);
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> midlertidigLonnstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_VarigLønnstilskudd() {
        Fnr deltakerFnr = new Fnr("12085220754");
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        VarigLonnstilskuddStartOgSluttDatoStrategy varigLonnstilskuddStartOgSluttDatoStrategy = new VarigLonnstilskuddStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> varigLonnstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }
}
