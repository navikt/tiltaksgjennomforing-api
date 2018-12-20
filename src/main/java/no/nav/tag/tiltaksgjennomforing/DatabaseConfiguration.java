package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.lang.Nullable;

import java.util.Arrays;

@Configuration
public class DatabaseConfiguration extends JdbcConfiguration {
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(new Converter<Fnr, String>() {
            @Nullable
            @Override
            public String convert(Fnr in) {
                return in.getFnr();
            }
        }, new Converter<NavIdent, String>() {
            @Nullable
            @Override
            public String convert(NavIdent in) {
                return in.getId();
            }
        }, new Converter<String, NavIdent>() {
            @Nullable
            @Override
            public NavIdent convert(String in) {
                return new NavIdent(in);
            }
        }));
    }
}
