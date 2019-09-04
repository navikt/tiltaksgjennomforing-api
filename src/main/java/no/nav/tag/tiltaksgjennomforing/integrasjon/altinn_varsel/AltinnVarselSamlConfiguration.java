package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_varsel;

import lombok.RequiredArgsConstructor;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.nav.tag.tiltaksgjennomforing.integrasjon.WsClient;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnVarselProperties;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.StsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"preprod", "prod"})
@RequiredArgsConstructor
public class AltinnVarselSamlConfiguration {
    private final AltinnVarselProperties varselProperties;
    private final StsProperties stsProperties;

    @Bean
    public INotificationAgencyExternalBasic iNotificationAgencyExternalBasic() {
        var port = new WsClient<INotificationAgencyExternalBasic>().createPort(varselProperties.getUri().toString(),
                INotificationAgencyExternalBasic.class,
                true);
        STSClientConfigurer configurer = new STSClientConfigurer(stsProperties.getUri(), stsProperties.getUsername(), stsProperties.getPassword());
        configurer.configureRequestSamlToken(port);
        return port;
    }
}
