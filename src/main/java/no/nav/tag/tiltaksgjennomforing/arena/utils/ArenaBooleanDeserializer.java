package no.nav.tag.tiltaksgjennomforing.arena.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ArenaBooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        return "J".equals(jsonParser.getText());
    }
}
