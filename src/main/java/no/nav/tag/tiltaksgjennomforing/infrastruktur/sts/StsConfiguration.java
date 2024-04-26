package no.nav.tag.tiltaksgjennomforing.infrastruktur.sts;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class StsConfiguration {
    @Bean
    public RestTemplate stsRestTemplate(STSClient stsClient) {
        return new RestTemplateBuilder()
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(stsClient.hentSTSToken().getAccessToken());
                    return execution.execute(request, body);
                })
                .build();
    }
}
