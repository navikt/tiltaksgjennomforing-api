package no.nav.security.oidc.test.support;

/*
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 */

import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.Resource;
import no.nav.security.oidc.configuration.OIDCResourceRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

public class FileResourceRetriever extends OIDCResourceRetriever {

    private final String metadataSelvbetjeningFile;
    private final String metadataIssoFile;
    private final String jwksFile;

    public FileResourceRetriever(String metadataSelvbetjeningFile, String metadataIssoFile, String jwksFile) {
        this.metadataSelvbetjeningFile = metadataSelvbetjeningFile;
        this.metadataIssoFile = metadataIssoFile;
        this.jwksFile = jwksFile;
    }

    @Override
    public Resource retrieveResource(URL url) throws IOException {
        String content = getContentFromFile(url);
        return new Resource(content, "application/json");
    }

    private String getContentFromFile(URL url) {
        try {
            if (url.toString().contains("metadata-isso")) {
                return IOUtils.readInputStreamToString(getInputStream(metadataIssoFile), Charset.forName("UTF-8"));
            }
            if (url.toString().contains("metadata-selvbetjening")) {
                return IOUtils.readInputStreamToString(getInputStream(metadataSelvbetjeningFile), Charset.forName("UTF-8"));
            }
            if (url.toString().contains("jwks")) {
                return IOUtils.readInputStreamToString(getInputStream(jwksFile), Charset.forName("UTF-8"));
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getInputStream(String file) throws IOException {
        return FileResourceRetriever.class.getResourceAsStream(file);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " [metadataIssoFile=" + metadataIssoFile
                + ", metadataSelvbetjeningFile=" + metadataSelvbetjeningFile
                + ", jwksFile=" + jwksFile + "]";
    }
}
