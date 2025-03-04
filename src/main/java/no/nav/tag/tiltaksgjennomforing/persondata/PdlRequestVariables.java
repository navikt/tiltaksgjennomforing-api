package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;

import java.util.List;

public class PdlRequestVariables {

    @Value
    public static class Ident {
        private final String ident;
    }

    @Value
    public static class IdentBolk {
        private final List<String> identer;
    }

}
