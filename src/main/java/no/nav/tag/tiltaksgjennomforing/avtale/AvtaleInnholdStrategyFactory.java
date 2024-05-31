package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AvtaleInnholdStrategyFactory {
    public AvtaleInnholdStrategy create(AvtaleInnhold avtaleInnhold, Tiltakstype tiltakstype) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING -> new ArbeidstreningStrategy(avtaleInnhold);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddStrategy(avtaleInnhold);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddStrategy(avtaleInnhold);
            case MENTOR -> new MentorStrategy(avtaleInnhold);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStrategy(avtaleInnhold);
            case SOMMERJOBB -> new SommerjobbStrategy(avtaleInnhold);
            case VTAO -> new VtaoStrategy(avtaleInnhold);
        };
    }
}
