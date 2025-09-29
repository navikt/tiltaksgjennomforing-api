package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AvtaleInnholdStrategyFactory {
    public AvtaleInnholdStrategy create(AvtaleInnhold avtaleInnhold, Tiltakstype tiltakstype) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING -> new ArbeidstreningStrategy(avtaleInnhold);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddAvtaleInnholdStrategy(avtaleInnhold);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleInnholdStrategy(avtaleInnhold);
            case MENTOR -> new MentorAvtaleInnholdStrategy(avtaleInnhold);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStrategy(avtaleInnhold);
            case SOMMERJOBB -> new SommerjobbAvtaleInnholdStrategy(avtaleInnhold);
            case VTAO -> new VtaoStrategy(avtaleInnhold);
        };
    }
}
