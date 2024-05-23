package no.nav.tag.tiltaksgjennomforing.avtale;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BedriftNrConverter implements AttributeConverter<BedriftNr, String> {

    @Override
    public String convertToDatabaseColumn(BedriftNr attribute) {
        return attribute.asString();
    }

    @Override
    public BedriftNr convertToEntityAttribute(String dbData) {
        return new BedriftNr(dbData);
    }
}
