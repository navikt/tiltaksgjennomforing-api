package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

public enum NotifikasjonMerkelapp {
    LONNTILSKUDD("Lønnstilskudd"),
    MENTOR("Mentor"),
    SOMMERJOBB("Sommerjobb"),
    INKLUDERINGSTILSKUDD("Inkluderingstilskudd"),
    ARBEIDSTRENING("Arbeidstrening");

    private final String merkelapp;

    NotifikasjonMerkelapp(String merkelapp) {
        this.merkelapp = merkelapp;
    }

    public String getValue() {
        return merkelapp;
    }

    public static NotifikasjonMerkelapp getMerkelapp(String merkelapp) {
        return switch (merkelapp) {
            case "Midlertidig lønnstilskudd", "Varig lønnstilskudd" -> NotifikasjonMerkelapp.LONNTILSKUDD;
            case "Mentor" -> NotifikasjonMerkelapp.MENTOR;
            case "Sommerjobb" -> NotifikasjonMerkelapp.SOMMERJOBB;
            case "Inkluderingstilskudd" -> NotifikasjonMerkelapp.INKLUDERINGSTILSKUDD;
            default -> NotifikasjonMerkelapp.ARBEIDSTRENING;
        };
    }
}
