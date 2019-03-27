package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "altinn")
public class AltinnConfiguration {
    private String apikey;
    private String gw_apikey;
    private String altinn_url;

    public String getApikey() {
        return apikey;
    }

    public String getGw_apikey() {
        return gw_apikey;
    }

    public String getAltinn_url() {
        return altinn_url;
    }
}
