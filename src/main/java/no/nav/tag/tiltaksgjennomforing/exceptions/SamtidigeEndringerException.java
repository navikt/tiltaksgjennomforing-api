package no.nav.tag.tiltaksgjennomforing.exceptions;

public class SamtidigeEndringerException extends FeilkodeException {

    @Override
    String feilkode() {
        return "SAMTIDIGE_ENDRINGER";
    }
}
