package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStartOgSluttDatoStrategy implements StartOgSluttDatoStrategy {
    private final Kvalifiseringsgruppe kvalifiseringsgruppe;
    int TOLV_MND_MAKS_LENGDE = 12;
    int TJUEFIRE_MND_MAKS_LENGDE = 24;

    MidlertidigLonnstilskuddStartOgSluttDatoStrategy(Kvalifiseringsgruppe kvalifiseringsgruppe) {
        this.kvalifiseringsgruppe = kvalifiseringsgruppe;
    }

    private Integer settMakslengdeUtIfraKvalifiseringsgruppe() {
        if (kvalifiseringsgruppe != null) {
            return this.kvalifiseringsgruppe == Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS ?
                    TOLV_MND_MAKS_LENGDE : TJUEFIRE_MND_MAKS_LENGDE;
        }
        return TOLV_MND_MAKS_LENGDE;
    }

    @Override
    public void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategy.super.sjekkStartOgSluttDato(startDato, sluttDato);
        if (startDato != null && sluttDato != null && startDato.plusMonths(settMakslengdeUtIfraKvalifiseringsgruppe())
                .isBefore(sluttDato)) {
            throw new VarighetForLangMidlertidigLonnstilskuddException();
        }
    }
}
