package no.nav.tag.tiltaksgjennomforing.exceptions;

public class IkkeValgtPartException extends FeilkodeException {


    @Override
    String feilkode() {
        return "IKKE_VALGT_PART";
    }
}
