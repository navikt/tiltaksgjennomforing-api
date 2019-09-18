package no.nav.tag.tiltaksgjennomforing.avtale;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class NavIdentConverter implements AttributeConverter<NavIdent, String> {

    @Override
    public String convertToDatabaseColumn(NavIdent attribute) {
        return attribute.asString();
    }

    @Override
    public NavIdent convertToEntityAttribute(String dbData) {
        return new NavIdent(dbData);
    }
}
