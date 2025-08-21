package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    private static final int TJUEFIRE_MND_MAKS_LENGDE = 24;
    private static final int TOLV_MND_MAKS_LENGDE = 12;
    private final Kvalifiseringsgruppe kvalifiseringsgruppe;

    MidlertidigLonnstilskuddStartOgSluttDatoStrategy(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato, boolean erGodkjentForEtterregistrering, boolean erAvtaleInngått, Fnr deltakerFnr) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering, erAvtaleInngått, deltakerFnr);

        if (sluttDato == null) {
            return;
        }
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato == null) {
            return;
        }

        boolean erSpesieltTilpassetInnsats = kvalifiseringsgruppe == Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS;
        boolean erVarigTilpassetInnsats = kvalifiseringsgruppe == Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS;
        boolean erSituasjonsbestemtInnsats = kvalifiseringsgruppe == Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS;

        if (
            (erSpesieltTilpassetInnsats || erVarigTilpassetInnsats) &&
            startDato.plusMonths(TJUEFIRE_MND_MAKS_LENGDE).minusDays(1).isBefore(sluttDato)
        ) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_24_MND);
        }

        if (
            erSituasjonsbestemtInnsats &&
            startDato.plusMonths(TOLV_MND_MAKS_LENGDE).minusDays(1).isBefore(sluttDato)
        ) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_12_MND);
        }

        // Ikke funnet kvalifiseringsgruppe, default 12 mnd
        if (kvalifiseringsgruppe == null && startDato.plusMonths(TOLV_MND_MAKS_LENGDE)
            .minusDays(1)
            .isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_12_MND);
        }
    }
}
