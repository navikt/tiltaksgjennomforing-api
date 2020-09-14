package no.nav.tag.tiltaksgjennomforing.exceptions;

public class AltMåVæreFyltUtException extends FeilkodeException {

    @Override
    String feilkode() {
        return "ALT_MA_VAERE_FYLT_UT";
    }
}