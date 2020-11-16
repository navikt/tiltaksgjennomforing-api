package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

public class Topics {
    public static final String STATISTIKKFORMIDLING = "privat-tiltaksgjennomforing-statistikkformidling";
    public static final String SMS_VARSEL = "privat-tiltaksgjennomforing-smsVarsel";
    public static final String SMS_VARSEL_RESULTAT = "privat-tiltaksgjennomforing-smsVarselResultat";

    public static String[] alleTopics() {
        return new String[]{
            STATISTIKKFORMIDLING,
            SMS_VARSEL,
            SMS_VARSEL_RESULTAT
        };
    }
}
