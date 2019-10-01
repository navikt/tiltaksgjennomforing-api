package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.nimbusds.jwt.JWTClaimsSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

import org.springframework.stereotype.Component;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.*;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenUtils {
        
    private static final String ACR = "acr";
    private static final String LEVEL4 = "Level4";

    static enum Issuer {
        
        ISSUER_ISSO("isso"),
        ISSUER_SELVBETJENING("selvbetjening"),
        ISSUER_SYSTEM("system");

        final String issuerName;
        
        Issuer(String issuerName) {
            this.issuerName = issuerName;
        }
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static class BrukerOgIssuer {
        
        private final Issuer issuer;
        private final String brukerIdent;
        
    }

    private final OIDCRequestContextHolder contextHolder;

    public Optional<BrukerOgIssuer> hentBrukerOgIssuer() {
        return hentClaim(ISSUER_SELVBETJENING, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SELVBETJENING, sub))
                .or(() -> hentClaim(ISSUER_ISSO, "NAVident").map(sub -> new BrukerOgIssuer(ISSUER_ISSO, sub)))
                .or(() -> hentClaim(ISSUER_SYSTEM, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SYSTEM, sub)));
    }

    private Optional<String> hentClaim(Issuer issuer, String claim) {
        return hentClaimSet(issuer).filter(jwtClaimsSet -> innloggingsNivaOK(issuer, jwtClaimsSet)).map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private boolean innloggingsNivaOK(Issuer issuer, JWTClaimsSet jwtClaimsSet) {
        return issuer != ISSUER_SELVBETJENING || LEVEL4.equals(jwtClaimsSet.getClaim(ACR));
    }

    private Optional<JWTClaimsSet> hentClaimSet(Issuer issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer.issuerName))
                .map(claims -> claims.getClaimSet());
    }

}
    