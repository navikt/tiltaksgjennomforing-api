package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

@Value
public class PdlRequest {
    private final String query;
    private final PdlRequestVariables variables;
}
