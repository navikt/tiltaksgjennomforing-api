package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public class StartOgSluttDatoStrategyFactory {
    public static StartOgSluttDatoStrategy create(Avtale avtale) {
        return switch (avtale.getTiltakstype()) {
            case ARBEIDSTRENING -> new ArbeidstreningStartOgSluttDatoStrategy(avtale);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddStartOgSluttDatoStrategy(avtale);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddStartOgSluttDatoStrategy(avtale);
            case MENTOR -> new MentorStartOgSluttDatoStrategy(avtale);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStartOgSluttDatoStrategy(avtale);
            case SOMMERJOBB -> new SommerjobbStartOgSluttDatoStrategy(avtale);
            case VTAO -> new VtaoStartOgSluttDatoStrategy(avtale);
            case FIREARIG_LONNSTILSKUDD -> new FirearigLonnstilskuddStartOgSluttDatoStrategy(avtale);
        };
    }
}
