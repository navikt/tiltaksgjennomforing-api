package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.INKLUDERINGSTILSKUDD;

@SyntetiskFnr
class InkluderingstilskuddStartOgSluttDatoStrategyTest {

    @Test
    public void Deltaker_er_for_gammel_for_å_gå_på_Inkluderingstilskudd() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(Fnr.generer(1952,8,12), TestData.etBedriftNr(), INKLUDERINGSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        InkluderingstilskuddStartOgSluttdatoStrategy inkluderingstilskuddStartOgSluttDatoStrategy = new InkluderingstilskuddStartOgSluttdatoStrategy(avtale);
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> inkluderingstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt));
    }
}
