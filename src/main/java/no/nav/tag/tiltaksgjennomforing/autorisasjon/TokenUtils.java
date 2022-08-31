package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.*;

@Component
@RequiredArgsConstructor
public class TokenUtils {
    private static final String ACR = "acr";
    private static final String LEVEL4 = "Level4";

   public enum Issuer {
        ISSUER_ISSO("isso"),
        ISSUER_SELVBETJENING("selvbetjening"),
        ISSUER_SYSTEM("system"),
        ISSUER_TOKENX("tokenx");

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

    private final TokenValidationContextHolder contextHolder;

    public Optional<BrukerOgIssuer> hentBrukerOgIssuer() {
        return hentClaim(ISSUER_SYSTEM, "sub").map(sub -> new BrukerOgIssuer(ISSUER_SYSTEM, sub))
            .or(() -> hentClaim(ISSUER_SELVBETJENING, "pid").map(sub -> new BrukerOgIssuer(ISSUER_SELVBETJENING, sub)))
            .or(() -> hentClaim(ISSUER_ISSO, "NAVident").map(sub -> new BrukerOgIssuer(ISSUER_ISSO, sub)))
            .or(() -> hentClaim(ISSUER_TOKENX, "pid").map(it -> new BrukerOgIssuer(ISSUER_TOKENX, it)));
    }

    public boolean harAdGruppe(UUID gruppeAD) {
        Optional<List<String>> groupsClaim = hentClaims(ISSUER_ISSO, "groups");
        if (!groupsClaim.isPresent()) {
            return false;
        }
        return groupsClaim.get().contains(gruppeAD.toString());
    }

    private Optional<List<String>> hentClaims(Issuer issuer, String claim) {
        return hentClaimSet(issuer).filter(jwtClaimsSet -> innloggingsNivaOK(issuer, jwtClaimsSet))
                .map(jwtClaimsSet -> (List<String>) jwtClaimsSet.get(claim));
    }

    private Optional<String> hentClaim(Issuer issuer, String claim) {
        return hentClaimSet(issuer).filter(jwtClaimsSet -> innloggingsNivaOK(issuer, jwtClaimsSet))
            .map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.get(claim)));
    }

    private boolean innloggingsNivaOK(Issuer issuer, JwtTokenClaims jwtClaimsSet) {

        return issuer != ISSUER_SELVBETJENING || LEVEL4.equals(jwtClaimsSet.get(ACR));
    }

    private Optional<JwtTokenClaims> hentClaimSet(Issuer issuer) {
        TokenValidationContext tokenValidationContext;
        try {
            tokenValidationContext = contextHolder.getTokenValidationContext();
        } catch (IllegalStateException e) {
            // Er ikke i kontekst av en request
            return Optional.empty();
        }
        return Optional.ofNullable(tokenValidationContext.getClaims(issuer.issuerName));

    }

    public String hentSelvbetjeningToken() {
        return contextHolder.getTokenValidationContext().getJwtToken(ISSUER_SELVBETJENING.issuerName).getTokenAsString();
    }
    public String hentTokenx() {
        return contextHolder.getTokenValidationContext().getJwtToken(ISSUER_TOKENX.issuerName).getTokenAsString();
    }

}
