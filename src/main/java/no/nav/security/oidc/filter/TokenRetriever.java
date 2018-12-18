package no.nav.security.oidc.filter;
/*
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 */

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.configuration.MultiIssuerConfiguration;
import no.nav.security.oidc.context.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TokenRetriever {

    private static Logger logger = LoggerFactory.getLogger(TokenRetriever.class);

    public static List<TokenContext> retrieveTokens(MultiIssuerConfiguration config, HttpServletRequest request) {
        var tokens = new HashMap<String, TokenContext>();

        logger.debug("checking authorization header ...");
        String auth = request.getHeader(OIDCConstants.AUTHORIZATION_HEADER);
        if (auth != null) {
            String[] authElements = auth.split(",");
            for (String authElement : authElements) {
                try {
                    String[] pair = authElement.split(" ");
                    if (pair[0].trim().equalsIgnoreCase("bearer")) {
                        String token = pair[1].trim();
                        createTokenContext(config, token)
                                .ifPresent(tokenContext -> {
                                    tokens.put(tokenContext.getIssuer(), tokenContext);
                                    logger.debug("found token for issuer {}. adding new unvalidated tokencontext.", tokenContext.getIssuer());
                                });
                    }
                } catch (Exception e) {
                    // log, ignore and jump to next
                    logger.warn("Failed to parse Authorization header: " + e.toString(), e);
                }
            }
        }

        logger.debug("checking for tokens in cookies ...");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (String issuer : config.getIssuerShortNames()) {
                String expectedName = config.getIssuer(issuer).getCookieName();
                expectedName = expectedName == null ? OIDCConstants.getDefaultCookieName(issuer) : expectedName;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase(expectedName)) {
                        logger.debug("found cookie with expected name {}", expectedName);
                        createTokenContext(config, cookie.getValue())
                                .ifPresentOrElse(tokenContext -> {
                                    tokens.put(tokenContext.getIssuer(), tokenContext);
                                    logger.debug("found token for issuer {}. adding new unvalidated tokencontext.", tokenContext.getIssuer());
                                }, () -> logger.debug("could not find token for issuer {}", issuer));
                    }
                }
            }
        }

        return new ArrayList<>(tokens.values());
    }

    private static Optional<TokenContext> createTokenContext(MultiIssuerConfiguration config, String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            if (config.getIssuer(jwt.getJWTClaimsSet().getIssuer()) != null) {
                String issuer = config.getIssuer(jwt.getJWTClaimsSet().getIssuer()).getName();
                return Optional.of(new TokenContext(issuer, token));
            }
            return Optional.empty();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
