package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class HentPersonBolk {
    private final String ident;
    private final HentPerson[] person;
    private final String code;
}
