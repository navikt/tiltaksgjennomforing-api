package no.nav.tag.tiltaksgjennomforing.integrasjon;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import java.util.Arrays;
import java.util.Objects;

public class WsClient<T> {

    @SuppressWarnings("unchecked")
    public T createPort(String serviceUrl, Class<?> portType, boolean inkluderWSAddressing, PhaseInterceptor<? extends Message>... interceptors) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(portType);
        factory.setAddress(Objects.requireNonNull(serviceUrl));
        if (inkluderWSAddressing) {
            factory.getFeatures().add(new WSAddressingFeature());
        }
        factory.getFeatures().add(new LoggingFeature());
        T port = (T) factory.create();
        Client client = ClientProxy.getClient(port);
        Arrays.stream(interceptors).forEach(client.getOutInterceptors()::add);
        return port;
    }

}
