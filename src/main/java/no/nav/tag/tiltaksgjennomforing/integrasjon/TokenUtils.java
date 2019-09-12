package no.nav.tag.tiltaksgjennomforing.integrasjon;

import com.nimbusds.jwt.JWTClaimsSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.context.OIDCRequestContextHolder;

import org.springframework.stereotype.Component;

import static no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils.Issuer.*;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenUtils {
        
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
        return hentClaim(ISSUER_SELVBETJENING.issuerName, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SELVBETJENING, sub))
                .or(() -> hentClaim(ISSUER_ISSO.issuerName, "NAVident").map(sub -> new BrukerOgIssuer(ISSUER_ISSO, sub)))
                .or(() -> hentClaim(ISSUER_SYSTEM.issuerName, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SYSTEM, sub)));
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        return hentClaimSet(issuer).map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(claims -> claims.getClaimSet());
    }

}
    