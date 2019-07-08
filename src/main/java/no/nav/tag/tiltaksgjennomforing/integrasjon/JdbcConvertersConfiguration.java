package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class JdbcConvertersConfiguration extends JdbcConfiguration {
    @Bean
    public DataSourceTransactionManager dstm(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

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
