package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class HentPerson {
    private final Adressebeskyttelse[] adressebeskyttelse;
    private final Folkeregisteridentifikator[] folkeregisteridentifikator;
    private final Navn[] navn;
}
