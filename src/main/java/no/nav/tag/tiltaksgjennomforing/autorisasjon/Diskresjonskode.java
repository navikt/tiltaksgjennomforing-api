package no.nav.tag.tiltaksgjennomforing.autorisasjon;

public enum Diskresjonskode {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT;

    public static Diskresjonskode parse(String str) {
        return switch (str) {
            case "STRENGT_FORTROLIG_UTLAND" -> STRENGT_FORTROLIG_UTLAND;
            case "STRENGT_FORTROLIG" -> STRENGT_FORTROLIG;
            case "FORTROLIG" -> FORTROLIG;
            case "UGRADERT" -> UGRADERT;
            default -> throw new IllegalArgumentException("Ukjent diskresjonskode: " + str);
        };
    }
}
