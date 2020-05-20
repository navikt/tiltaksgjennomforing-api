package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.*;

@Component
@RequiredArgsConstructor
public class TokenUtils {
    private static final String ACR = "acr";
    private static final String LEVEL4 = "Level4";

    enum Issuer {
        ISSUER_ISSO("isso"),
        ISSUER_SELVBETJENING("selvbetjening"),
        ISSUER_SYSTEM("system");

        final String issuerName;

        Issuer(String issuerName) {
            this.issuerName = issuerName;
        }
    }

    @Value
    public static class BrukerOgIssuer {
        Issuer issuer;
        String brukerIdent;
    }

    private final OIDCRequestContextHolder contextHolder;

    public Optional<BrukerOgIssuer> hentBrukerOgIssuer() {
        return hentClaim(ISSUER_SYSTEM, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SYSTEM, sub))
                .or(() -> hentClaim(ISSUER_SELVBETJENING, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SELVBETJENING, sub)))
                .or(() -> hentClaim(ISSUER_ISSO, "NAVident").map(sub -> new BrukerOgIssuer(ISSUER_ISSO, sub)));
    }

    private Optional<String> hentClaim(Issuer issuer, String claim) {
        return hentClaimSet(issuer).filter(jwtClaimsSet -> innloggingsNivaOK(issuer, jwtClaimsSet)).map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private boolean innloggingsNivaOK(Issuer issuer, JWTClaimsSet jwtClaimsSet) {
        return issuer != ISSUER_SELVBETJENING || LEVEL4.equals(jwtClaimsSet.getClaim(ACR));
    }

    private Optional<JWTClaimsSet> hentClaimSet(Issuer issuer) {
        OIDCValidationContext oidcValidationContext;
        try {
            oidcValidationContext = contextHolder.getOIDCValidationContext();
        } catch (IllegalStateException e) {
            // Er ikke i kontekst av en request
            return Optional.empty();
        }
        return Optional.ofNullable(oidcValidationContext.getClaims(issuer.issuerName))
                .map(claims -> claims.getClaimSet());
    }

    public String hentSelvbetjeningToken() {
        return contextHolder.getOIDCValidationContext().getToken(ISSUER_SELVBETJENING.issuerName).getIdToken();
    }
    public TokenContext hentSelvbetjeningTokenContext() {
        return contextHolder.getOIDCValidationContext().getToken(ISSUER_SELVBETJENING.issuerName);
    }
}
