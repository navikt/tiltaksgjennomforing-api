package no.nav.tag.tiltaksgjennomforing.avtale;

public class AnnullertGrunn {
    public static final String FEILREGISTRERING = "Feilregistrering";
    public static final String BEGYNT_I_ARBEID = "Begynt i arbeid";
    public static final String FÅTT_TILBUD_OM_ANNET_TILTAK = "Fått tilbud om annet tiltak";
    public static final String SYK = "Syk";
    public static final String IKKE_MØTT = "Ikke møtt";
    public static final String UTLØPT = "Utløpt";
    public static final String ANNULLERT_I_ARENA = "Avtalen er annullert i Arena";
    public static final String FINNES_IKKE_I_ARENA = "Avtalen finnes ikke i Arena";

    public static boolean skalFeilregistreres(String annullertGrunn) {
        return switch (annullertGrunn) {
            case FEILREGISTRERING, UTLØPT -> true;
            default -> false;
        };
    }
}
