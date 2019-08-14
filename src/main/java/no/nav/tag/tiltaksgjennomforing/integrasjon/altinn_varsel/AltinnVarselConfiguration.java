package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_varsel;

import lombok.RequiredArgsConstructor;
import no.altinn.services.serviceengine.notification._2010._10.INotificationAgencyExternalBasic;
import no.nav.tag.tiltaksgjennomforing.integrasjon.WsClient;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnVarselProperties;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.StsProperties;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT;

@Configuration
@RequiredArgsConstructor
public class AltinnVarselConfiguration {
    private static final String SAML_POLICY = "classpath:sts/policies/requestSamlPolicy.xml";
    private static final String UNT_POLICY = "classpath:sts/policies/untPolicy.xml";
    private final AltinnVarselProperties varselProperties;
    private final StsProperties stsProperties;

    @Bean
    public INotificationAgencyExternalBasic iNotificationAgencyExternalBasic() {
        var port = new WsClient<INotificationAgencyExternalBasic>().createPort(varselProperties.getUri().toString(), INotificationAgencyExternalBasic.class, Collections.singletonList(new LogErrorHandler()), true);
        configureSTSFor(port);
        return port;
    }

    private STSClient createSystemUserSTSClient(Client client) {
        STSClient stsClient = new STSClient(client.getBus());
        stsClient.setLocation(stsProperties.getUri().toString());
        stsClient.setProperties(Map.of(
                SecurityConstants.USERNAME, stsProperties.getUsername(),
                SecurityConstants.PASSWORD, stsProperties.getPassword()
        ));
        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setFeatures(Arrays.asList(new LoggingFeature()));
        stsClient.setPolicy(UNT_POLICY);
        stsClient.getRequestContext().put(CACHE_ISSUED_TOKEN_IN_ENDPOINT, true);

        var policyEngine = client.getBus().getExtension(PolicyEngine.class);
        var endpointInfo = client.getEndpoint().getEndpointInfo();
        var soapMessage = new SoapMessage(Soap12.getInstance());
        var endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, soapMessage);
        var policy = new RemoteReferenceResolver("", client.getBus().getExtension(PolicyBuilder.class)).resolveReference(SAML_POLICY);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, soapMessage));
        return stsClient;
    }

    private void configureSTSFor(INotificationAgencyExternalBasic port) {
        var client = ClientProxy.getClient(port);
        var stsClient = createSystemUserSTSClient(client);
        client.getRequestContext().put(SecurityConstants.STS_CLIENT, stsClient);
    }
}
