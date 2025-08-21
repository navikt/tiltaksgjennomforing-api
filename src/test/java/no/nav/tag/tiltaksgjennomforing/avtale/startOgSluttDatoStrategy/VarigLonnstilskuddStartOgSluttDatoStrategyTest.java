package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;

class VarigLonnstilskuddStartOgSluttDatoStrategyTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Inkluderingstilskudd() {
        Fnr deltakerFnr = Fnr.generer(1952,8,12);
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        Kvalifiseringsgruppe kvalifiseringsgruppe = Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS;
        VarigLonnstilskuddStartOgSluttDatoStrategy varigLonnstilskuddStartOgSluttDatoStrategy = new VarigLonnstilskuddStartOgSluttDatoStrategy();
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> varigLonnstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }
}
