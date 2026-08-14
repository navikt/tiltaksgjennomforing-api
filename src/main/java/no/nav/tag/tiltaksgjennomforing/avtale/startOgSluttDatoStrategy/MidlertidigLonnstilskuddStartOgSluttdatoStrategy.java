package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStartOgSluttdatoStrategy extends StartOgSluttdatoStrategy {
    private static final int TJUEFIRE_MND_MAKS_LENGDE = 24;
    private static final int TOLV_MND_MAKS_LENGDE = 12;

    public MidlertidigLonnstilskuddStartOgSluttdatoStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void sjekkStartOgSluttdato(Stillingstype stillingstype, LocalDate startDato, LocalDate sluttDato) {
        super.sjekkStartOgSluttdato(stillingstype, startDato, sluttDato);

        if (sluttDato == null) {
            return;
        }
        Fnr deltakerFnr = avtale.getDeltakerFnr();
        if (deltakerFnr != null && deltakerFnr.erOver72ÅrFraSluttDato(sluttDato)) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (startDato == null) {
            return;
        }

        Innsatsgruppe innsatsgruppe = avtale.getInnsatsgruppe();
        boolean erSpesieltTilpassetInnsats = innsatsgruppe == Innsatsgruppe.TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE;
        boolean erVarigTilpassetInnsats = innsatsgruppe == Innsatsgruppe.LITEN_MULIGHET_TIL_A_JOBBE || innsatsgruppe == Innsatsgruppe.JOBBE_DELVIS;
        boolean erSituasjonsbestemtInnsats = innsatsgruppe == Innsatsgruppe.TRENGER_VEILEDNING;

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

        // Ikke funnet innsatsgruppe, default 12 mnd
        if (avtale.getInnsatsgruppe() == null && startDato.plusMonths(TOLV_MND_MAKS_LENGDE)
            .minusDays(1)
            .isBefore(sluttDato)) {
            throw new FeilkodeException(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_12_MND);
        }
    }
}
