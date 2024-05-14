package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IdentifikatorConverter implements AttributeConverter<Identifikator, String> {

    @Override
    public String convertToDatabaseColumn(Identifikator attribute) {
        if(attribute == null){
            return null;
        }
        return attribute.asString();
    }

    @Override
    public Identifikator convertToEntityAttribute(String dbData) {
        return new Identifikator(dbData);
    }
}
