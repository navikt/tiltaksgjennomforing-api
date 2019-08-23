package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

public class Topics {
    public static final String SMS_VARSEL = "privat-tiltaksgjennomforing-smsVarsel";
    public static final String SMS_VARSEL_RESULTAT = "privat-tiltaksgjennomforing-smsVarselResultat";
    public static final String AVTALE_GODKJENT = "privat-tiltaksgjennomforing-godkjentAvtale";

    public static String[] alleTopics() {
        return new String[]{
                SMS_VARSEL,
                SMS_VARSEL_RESULTAT,
                AVTALE_GODKJENT
        };
    }
}
