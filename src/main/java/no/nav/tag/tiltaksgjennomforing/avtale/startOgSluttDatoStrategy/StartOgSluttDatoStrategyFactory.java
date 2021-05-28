package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

@UtilityClass
public class StartOgSluttDatoStrategyFactory {
    public static StartOgSluttDatoStrategy create(Tiltakstype tiltakstype) {
        switch (tiltakstype) {
            case ARBEIDSTRENING:
                return new ArbeidstreningStartOgSluttDatoStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD:
                return new MidlertidigLonnstilskuddStartOgSluttDatoStrategy();
            case VARIG_LONNSTILSKUDD:
                return new VarigLonnstilskuddStartOgSluttDatoStrategy();
            case MENTOR:
                return new MentorStartOgSluttDatoStrategy();
            case SOMMERJOBB:
                return new SommerjobbStartOgSluttDatoStrategy();
        }
        return new StartOgSluttDatoStrategy() {
        };
    }
}
