package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class HentPersonBolk {
    private final String ident;
    private final HentPerson[] hentPerson;
    private final String code;
}
