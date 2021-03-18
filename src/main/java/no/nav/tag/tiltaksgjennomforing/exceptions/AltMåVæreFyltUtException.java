package no.nav.tag.tiltaksgjennomforing.exceptions;

import java.util.Set;

public class AltMåVæreFyltUtException extends FeilkodeException {
    private final Set<String> felterSomIkkeErFyltUt;

    public AltMåVæreFyltUtException(Set<String> felterSomIkkeErFyltUt) {
        super(Feilkode.ALT_MA_VAERE_FYLT_UT);
        this.felterSomIkkeErFyltUt = felterSomIkkeErFyltUt;
    }

    public Set<String> getFelterSomIkkeErFyltUt() {
        return felterSomIkkeErFyltUt;
    }
}