package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class HentPersonBolk {
    private static final String OK = "ok";

    private final String ident;
    private final HentPerson person;
    private final String code;

    public boolean isOk() {
        return OK.equals(code);
    }
}
