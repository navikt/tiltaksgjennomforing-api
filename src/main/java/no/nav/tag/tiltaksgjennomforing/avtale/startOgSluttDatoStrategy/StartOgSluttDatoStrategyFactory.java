package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;

@UtilityClass
public class StartOgSluttDatoStrategyFactory {
    public static StartOgSluttDatoStrategy create(Tiltakstype tiltakstype, Kvalifiseringsgruppe kvalifiseringsgruppe) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING -> new ArbeidstreningStartOgSluttDatoStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD ->
                    new MidlertidigLonnstilskuddStartOgSluttDatoStrategy(kvalifiseringsgruppe);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddStartOgSluttDatoStrategy();
            case MENTOR -> new MentorStartOgSluttDatoStrategy(kvalifiseringsgruppe);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStartOgSluttDatoStrategy();
            case SOMMERJOBB -> new SommerjobbStartOgSluttDatoStrategy();
            case VTAO -> new VtaoStartOgSluttDatoStrategy();
        };
    }
}
