package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangArbeidstreningException;

import java.time.LocalDate;

public class ArbeidstreningStrategy extends BaseAvtaleInnholdStrategy {

    public ArbeidstreningStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        nyAvtale.getMaal().forEach(Maal::sjekkMaalLengde);
        avtaleInnhold.getMaal().clear();
        avtaleInnhold.getMaal().addAll(nyAvtale.getMaal());
        avtaleInnhold.getMaal().forEach(m -> m.setAvtaleInnhold(avtaleInnhold));
        super.endre(nyAvtale);
    }

    @Override
    public boolean erAltUtfylt() {
        return super.erAltUtfylt()
                && !avtaleInnhold.getMaal().isEmpty();
    }

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.plusMonths(18).isBefore(sluttDato)) {
            throw new VarighetForLangArbeidstreningException();
        }
    }
}
