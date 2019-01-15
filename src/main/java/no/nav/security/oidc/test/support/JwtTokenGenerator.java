package no.nav.security.oidc.test.support;

import com.nimbusds.jose.*;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JwtTokenGenerator {

    public static String ACR = "Level4";
    public static long EXPIRY = 60 * 60 * 3600;

    private JwtTokenGenerator() {
    }

    public static String signedJWTAsString(String subject, String issuer, String audience) {
        return createSignedJWT(subject, issuer, audience).serialize();
    }

    public static SignedJWT createSignedJWT(String subject, String issuer, String audience) {
        return createSignedJWT(subject, EXPIRY, new HashMap<>(), issuer, audience);
    }

    public static SignedJWT createSignedJWT(String subject, long expiryInMinutes, Map<String, Object> claims, String issuer, String audience) {
        JWTClaimsSet claimsSet = buildClaimSet(subject, issuer, audience, ACR, TimeUnit.MINUTES.toMillis(expiryInMinutes), claims);
        return createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet);
    }

    public static SignedJWT createSignedJWT(JWTClaimsSet claimsSet) {
        return createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet);
    }

    public static JWTClaimsSet buildClaimSet(
            String subject,
            String issuer,
            String audience,
            String authLevel,
            long expiry, Map<String, Object> additionalClaims
    ) {
        Date now = new Date();
        JWTClaimsSet.Builder claimSetBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(audience)
                .jwtID(UUID.randomUUID().toString())
                .claim("acr", authLevel)
                .claim("ver", "1.0")
                .claim("nonce", "myNonce")
                .claim("auth_time", now)
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + expiry));
        additionalClaims.keySet().forEach(key -> claimSetBuilder.claim(key, additionalClaims.get(key)));
        return claimSetBuilder.build();
    }

    protected static SignedJWT createSignedJWT(RSAKey rsaJwk, JWTClaimsSet claimsSet) {
        try {
            JWSHeader.Builder header = new Builder(JWSAlgorithm.RS256)
                    .keyID(rsaJwk.getKeyID())
                    .type(JOSEObjectType.JWT);

            SignedJWT signedJWT = new SignedJWT(header.build(), claimsSet);
            JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
            signedJWT.sign(signer);

            return signedJWT;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
