package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public enum Diskresjonskode {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT;

    public static Diskresjonskode parse(String str) {
        return switch (str) {
            case "STRENGT_FORTROLIG_UTLAND" -> STRENGT_FORTROLIG_UTLAND;
            case "STRENGT_FORTROLIG" -> STRENGT_FORTROLIG;
            case "FORTROLIG" -> FORTROLIG;
            case "UGRADERT" -> UGRADERT;
            case null, default -> UGRADERT;
        };
    }

    public boolean erKode6() {
        return STRENGT_FORTROLIG.equals(this) || STRENGT_FORTROLIG_UTLAND.equals(this);
    }

    public boolean erKode7() {
        return FORTROLIG.equals(this);
    }

    public boolean erKode6Eller7() {
        return erKode6() || erKode7();
    }

    public static class DiskresjonskodeDeserializer extends JsonDeserializer<Diskresjonskode> {
        @Override
        public Diskresjonskode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Diskresjonskode.parse(p.getValueAsString());
        }
    }
}
