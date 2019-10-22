package no.nav.tag.tiltaksgjennomforing.infrastruktur.database;

import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.lang.Nullable;

import java.util.Arrays;

@Configuration
public class JdbcConvertersConfiguration extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(new Converter<Identifikator, String>() {
            @Nullable
            @Override
            public String convert(Identifikator in) {
                return in.asString();
            }
        }, new Converter<String, Identifikator>() {
            @Nullable
            @Override
            public Identifikator convert(String in) {
                return new Identifikator(in);
            }
        }));
    }
}
