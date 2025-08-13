package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;

class MidlertidigLonnstilskuddStartOgSluttDatoStrategyTest {
    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Inkluderingstilskudd() {
        Fnr deltakerFnr = new Fnr("12085220754");
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        boolean erAvtaleInngått = true;
        boolean erGodkjentForEtterregistrering = true;
        Kvalifiseringsgruppe kvalifiseringsgruppe = Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS;
        MidlertidigLonnstilskuddStartOgSluttDatoStrategy midlertidigLonnstilskuddStartOgSluttDatoStrategy = new MidlertidigLonnstilskuddStartOgSluttDatoStrategy(kvalifiseringsgruppe);
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> midlertidigLonnstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttDato(avtaleStart, avtaleSlutt ,erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr));
    }
}
