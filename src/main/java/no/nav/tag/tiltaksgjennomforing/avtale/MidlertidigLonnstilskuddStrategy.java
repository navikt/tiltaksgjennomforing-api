package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;

public class MidlertidigLonnstilskuddStrategy extends LonnstilskuddStrategy {

    private static final int MAKSIMALT_ANTALL_MÅNEDER_VARIGHET = 24;

    public MidlertidigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    protected void sjekkStartogSluttDato(LocalDate startDato, LocalDate sluttDato) {
        super.startOgSluttDatoMedVarighetErSattRiktig(startDato, sluttDato, MAKSIMALT_ANTALL_MÅNEDER_VARIGHET);
    }
}
