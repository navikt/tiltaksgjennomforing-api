package no.nav.tag.tiltaksgjennomforing.arena.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class ArenaBooleanDeserializer extends ValueDeserializer<Boolean> {
    @Override
    public Boolean deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws JacksonException {
        return "J".equals(jsonParser.getText());
    }
}
