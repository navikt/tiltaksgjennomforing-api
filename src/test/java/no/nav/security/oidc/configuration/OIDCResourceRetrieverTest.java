package no.nav.security.oidc.configuration;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class OIDCResourceRetrieverTest {

    private OIDCResourceRetriever resourceRetriever = new OIDCResourceRetriever();

    @Test
    public void testUsePlainTextForHttps() throws MalformedURLException, URISyntaxException {
        resourceRetriever.setUsePlainTextForHttps(true);
        String scheme = "https://";
        String host = "host.domain.no";
        String pathAndQuery = "/somepath?foo=bar&bar=foo";
        URL url = URI.create(scheme + host + pathAndQuery).toURL();
        assertEquals("http://" + host + ":443" + pathAndQuery, resourceRetriever.urlWithPlainTextForHttps(url).toString());
    }

}
