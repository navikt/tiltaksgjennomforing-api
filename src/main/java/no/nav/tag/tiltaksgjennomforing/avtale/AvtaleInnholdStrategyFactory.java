package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;

@UtilityClass
public class AvtaleInnholdStrategyFactory {
    public AvtaleInnholdStrategy create(AvtaleInnhold avtaleInnhold, Tiltakstype tiltakstype) {
        switch (tiltakstype) {
            case ARBEIDSTRENING:
                return new ArbeidstreningStrategy(avtaleInnhold);
            case MIDLERTIDIG_LONNSTILSKUDD:
                return new MidlertidigLonnstilskuddStrategy(avtaleInnhold);
            case VARIG_LONNSTILSKUDD:
                return new VarigLonnstilskuddStrategy(avtaleInnhold);
            case MENTOR:
                return new MentorStrategy(avtaleInnhold);
            case SOMMERJOBB:
                return new SommerjobbStrategy(avtaleInnhold);
        }
        throw new IllegalStateException();
    }
}
