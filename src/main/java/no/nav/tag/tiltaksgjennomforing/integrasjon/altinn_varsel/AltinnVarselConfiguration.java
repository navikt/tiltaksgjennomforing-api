package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_varsel;

import lombok.RequiredArgsConstructor;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.nav.tag.tiltaksgjennomforing.integrasjon.WsClient;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnVarselProperties;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.StsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AltinnVarselConfiguration {
    private final AltinnVarselProperties varselProperties;
    private final StsProperties stsProperties;

    @Bean
    public INotificationAgencyExternalBasic iNotificationAgencyExternalBasic() {
        INotificationAgencyExternalBasic port = new WsClient<INotificationAgencyExternalBasic>().createPort(varselProperties.getUri().toString(),
                INotificationAgencyExternalBasic.class,
                true);
        new STSClientConfigurer(stsProperties.getUri(), stsProperties.getUsername(), stsProperties.getPassword())
                .configureRequestSamlToken(port);
        return port;
    }
}
