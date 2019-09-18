package no.nav.tag.tiltaksgjennomforing.varsel.altinnvarsel;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class STSClientConfigurer {
    // Only use no transportbinding on localhost, should use the requestSamlPolicy.xml with transport binding https
    // when in production.
    private static final String STS_REQUEST_SAML_POLICY = "classpath:sts/policies/requestSamlPolicyNoTransportBinding.xml";
    private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:sts/policies/untPolicy.xml";
    private URI stsUri;
    private String serviceUsername;
    private String servicePassword;

    STSClientConfigurer(URI stsUri, String serviceUsername, String servicePassword) {
        this.stsUri = stsUri;
        this.serviceUsername = serviceUsername;
        this.servicePassword = servicePassword;
    }

    private static void setEndpointPolicyReference(Client client) {
        Policy policy = resolvePolicyReference(client);
        setClientEndpointPolicy(client, policy);
    }

    private static Policy resolvePolicyReference(Client client) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(STSClientConfigurer.STS_REQUEST_SAML_POLICY);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();

        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        SoapMessage message = new SoapMessage(Soap12.getInstance());
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
    }

    void configureRequestSamlToken(Object port) {
        Client client = ClientProxy.getClient(port);
        // do not have onbehalfof token so cache token in endpoint
        configureStsRequestSamlToken(client);
    }

    private void configureStsRequestSamlToken(Client client) {
        // TODO: remove custom client when STS is updated to support the cxf client
        STSClient stsClient = new STSClientWSTrust13and14(client.getBus());
        configureStsWithPolicyForClient(stsClient, client);
    }

    private void configureStsWithPolicyForClient(STSClient stsClient, Client client) {
        configureSTSClient(stsClient);

        client.getRequestContext().put(org.apache.cxf.rt.security.SecurityConstants.STS_CLIENT, stsClient);
        client.getRequestContext().put(org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT,
                true);
        setEndpointPolicyReference(client);
    }

    private void configureSTSClient(STSClient stsClient) {
        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setLocation(stsUri.toString());
        // For debugging
        stsClient.setFeatures(List.of(new LoggingFeature()));

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(org.apache.cxf.rt.security.SecurityConstants.USERNAME, serviceUsername);
        properties.put(org.apache.cxf.rt.security.SecurityConstants.PASSWORD, servicePassword);

        stsClient.setProperties(properties);

        // used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
    }
}
