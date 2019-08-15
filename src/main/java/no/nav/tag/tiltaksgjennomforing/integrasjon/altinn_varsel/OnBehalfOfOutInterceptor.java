package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_varsel;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.rt.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

public class OnBehalfOfOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger logger = LoggerFactory.getLogger(OnBehalfOfOutInterceptor.class);

    public static final String REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE = "request.onbehalfof.tokentype";
    public static final String REQUEST_CONTEXT_ONBEHALFOF_TOKEN = "request.onbehalfof.token";

    public enum TokenType {
        OIDC("urn:ietf:params:oauth:token-type:jwt");

        public String valueType;

        TokenType(String valueType) {
            this.valueType = valueType;
        }
    }

    public OnBehalfOfOutInterceptor() {
        // This can be in any stage before the WS-SP interceptors
        // setup the STS client and issued token interceptor.
        super(Phase.SETUP);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        logger.debug("looking up OnBehalfOfToken from requestcontext with key:" + REQUEST_CONTEXT_ONBEHALFOF_TOKEN);
        String token = (String) message.get(REQUEST_CONTEXT_ONBEHALFOF_TOKEN);
        TokenType tokenType = (TokenType) message.get(REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE);

        if ((token != null) && (tokenType != null)) {
            byte[] tokenBytes = token.getBytes();
            String wrappedToken = wrapTokenForTransport(tokenBytes, tokenType);

            // This will make sure that the STS client puts the OnBehalfOf Element in the token issue request
            message.put(SecurityConstants.STS_TOKEN_ON_BEHALF_OF, createOnBehalfOfElement(wrappedToken));
        } else {
            logger.info("could not find OnBehalfOfToken token in requestcontext. do nothing");
            // TODO: there is choice here between failing or silently ignore adding of onbehalfof element which is up to
            // the user.
            // throw new RuntimeException("could not find OnBehalfOfToken token in requestcontext with key " +
            // REQUEST_CONTEXT_ONBEHALFOF_TOKEN);
        }
    }

    private String wrapTokenForTransport(byte[] token, TokenType tokenType) {
        switch (tokenType) {
        case OIDC:
            return wrapWithBinarySecurityToken(token, tokenType.valueType);
        default:
            throw new RuntimeException("unsupported token type:" + tokenType);
        }
    }

    private static Element createOnBehalfOfElement(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            new RuntimeException(e);
        }
        return null;
    }

    private static String wrapWithBinarySecurityToken(byte[] token, String valueType) {
        String base64encodedToken = Base64.getEncoder().encodeToString(token);
        return "<wsse:BinarySecurityToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
                + " EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\""
                + " ValueType=\"" + valueType + "\" >" + base64encodedToken + "</wsse:BinarySecurityToken>";
    }
}
