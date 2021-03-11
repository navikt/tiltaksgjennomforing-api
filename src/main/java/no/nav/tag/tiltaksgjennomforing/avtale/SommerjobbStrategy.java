package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;

public class SommerjobbStrategy extends LonnstilskuddStrategy {

    public SommerjobbStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null) {
            if (startDato.isBefore(LocalDate.of(2021, 6, 1))) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_TIDLIG);
            }
        }
        if (sluttDato != null) {
            if (sluttDato.isAfter(LocalDate.of(2021, 8, 31))) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_SENT);
            }
        }
        if (startDato != null && sluttDato != null) {
            if (startDato.plusWeeks(4).isBefore(sluttDato)) {
                throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET);
            }
        }
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();
        if (lonnstilskuddProsent != null && !(lonnstilskuddProsent == 75 || lonnstilskuddProsent == 50)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        super.endre(endreAvtale);
    }

}
