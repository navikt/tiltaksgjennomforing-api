package no.nav.tag.tiltaksgjennomforing.avtale;

public class NavIdent extends Identifikator {
    public NavIdent(String verdi) {
        super(verdi);
        if (!erNavIdent(verdi)) {
            throw new IllegalArgumentException("Er ikke en nav-ident");
        }
    }

    public static boolean erNavIdent(String verdi) {
        return verdi != null && verdi.matches("\\w\\d{6}");
    }
}
