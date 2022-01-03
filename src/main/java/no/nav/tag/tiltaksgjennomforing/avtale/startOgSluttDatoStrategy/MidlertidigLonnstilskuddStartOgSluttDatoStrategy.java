package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    private final Kvalifiseringsgruppe kvalifiseringsgruppe;

    MidlertidigLonnstilskuddStartOgSluttDatoStrategy(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
    }

    private Integer settMakslengdeUtIfraKvalifiseringsgruppe() {
        final int TOLV_MND_MAKS_LENGDE = 12;
        if (kvalifiseringsgruppe != null) {
            final int TJUEFIRE_MND_MAKS_LENGDE = 24;
            return this.kvalifiseringsgruppe == Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS ?
                    TOLV_MND_MAKS_LENGDE : TJUEFIRE_MND_MAKS_LENGDE;
        }
        return TOLV_MND_MAKS_LENGDE;
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato,boolean erGodkjentForEtterregistrering) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato, erGodkjentForEtterregistrering);
        if (startDato != null && sluttDato != null && startDato.plusMonths(settMakslengdeUtIfraKvalifiseringsgruppe())
                .isBefore(sluttDato)) {
            throw new VarighetForLangMidlertidigLonnstilskuddException();
        }
    }
}
