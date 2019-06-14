package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

public class Topics {
    public static final String VARSLBAR_HENDELSE_OPPSTAATT = "privat-tiltaksgjennomforing-varslbarHendelseOppstaatt";
    public static final String VARSLING_UTFØRT = "privat-tiltaksgjennomforing-varslingUtfoert";
    public static final String VARSLING_FEILET = "privat-tiltaksgjennomforing-varslingFeilet";

    public static String[] alleTopics() {
        return new String[]{
                VARSLBAR_HENDELSE_OPPSTAATT
        };
    }
}
