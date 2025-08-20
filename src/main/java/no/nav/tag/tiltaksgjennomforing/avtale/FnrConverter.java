package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FnrConverter implements AttributeConverter<Fnr, String> {

    @Override
    public String convertToDatabaseColumn(Fnr attribute) {
        if (attribute == null) return null;
        return attribute.asString();
    }

    @Override
    public Fnr convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Fnr.fraDb(dbData);
    }
}
