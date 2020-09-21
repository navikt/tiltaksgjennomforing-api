package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMidlertidigLonnstilskuddException;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStrategy extends LonnstilskuddStrategy {
    public MidlertidigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.plusMonths(24).isBefore(sluttDato)) {
            throw new VarighetForLangMidlertidigLonnstilskuddException();
        }
    }
}
