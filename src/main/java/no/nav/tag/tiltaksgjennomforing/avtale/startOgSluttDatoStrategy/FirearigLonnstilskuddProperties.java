package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Konfigurasjon for tidligste tillatte oppstartsdato for fireårig lønnstilskudd.
 * Dato settes per miljø i application-*.yaml (reell dato kun i prod, tidlig dato i dev/test
 * slik at dato-sjekken ikke blokkerer testing).
 *
 * <p>{@code @PostConstruct}/{@code getInstance()}-mønsteret brukes fordi denne verdien trengs i
 * {@link FirearigLonnstilskuddStartOgSluttDatoStrategy}, som opprettes fra en statisk fabrikk
 * kalt inne i JPA-entiteten {@code Avtale} — der vanlig Spring-injeksjon ikke er tilgjengelig.</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.firearig-oppstartsdato")
public class FirearigLonnstilskuddProperties {
    private LocalDate dato;

    private static FirearigLonnstilskuddProperties instance;

    @PostConstruct
    void init() {
        instance = this;
    }

    public static FirearigLonnstilskuddProperties getInstance() {
        return instance;
    }
}
