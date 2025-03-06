package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class Folkeregisteridentifikator {
    private final String identifikasjonsnummer;
    private final String status;
    private final String type;
}
