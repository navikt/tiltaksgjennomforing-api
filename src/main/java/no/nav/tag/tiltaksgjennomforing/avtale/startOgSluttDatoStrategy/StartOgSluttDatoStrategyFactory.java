package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;

public class StartOgSluttDatoStrategyFactory {
    public static StartOgSluttDatoStrategy create(Tiltakstype tiltakstype, Kvalifiseringsgruppe kvalifiseringsgruppe, Boolean erOpprettetEllerEndretAvArena) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING -> new ArbeidstreningStartOgSluttDatoStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD ->
                    new MidlertidigLonnstilskuddStartOgSluttDatoStrategy(kvalifiseringsgruppe);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddStartOgSluttDatoStrategy();
            case MENTOR -> new MentorStartOgSluttDatoStrategy(kvalifiseringsgruppe, erOpprettetEllerEndretAvArena);
            case INKLUDERINGSTILSKUDD -> new InkluderingstilskuddStartOgSluttDatoStrategy();
            case SOMMERJOBB -> new SommerjobbStartOgSluttDatoStrategy();
            case VTAO -> new VtaoStartOgSluttDatoStrategy();
        };
    }
}
