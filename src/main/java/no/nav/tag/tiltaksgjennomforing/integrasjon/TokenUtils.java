package no.nav.tag.tiltaksgjennomforing.integrasjon;

import com.nimbusds.jwt.JWTClaimsSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

import org.springframework.stereotype.Component;

import static no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils.Issuer.*;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenUtils {
    
//<<<<<<< HEAD
//    final static String ISSUER_ISSO = "isso";
//    final static String ISSUER_SELVBETJENING = "selvbetjening";
    final static String ISSUER_SYSTEM = "system";
//
//    private final OIDCRequestContextHolder contextHolder;
//
//    @Autowired
//    public TokenUtils(OIDCRequestContextHolder contextHolder) {
//        this.contextHolder = contextHolder;
//    }
//
//    public InnloggetBruker hentInnloggetBruker() {
//        if (erInnloggetNavAnsatt()) {
//            return hentInnloggetNavAnsatt();
//        } else if (erInnloggetSelvbetjeningBruker()) {
//            return hentInnloggetSelvbetjeningBruker();
//        } else {
//            throw new TilgangskontrollException("Bruker er ikke innlogget.");
//=======
    
    static enum Issuer {
        
        ISSUER_ISSO("isso"),
        ISSUER_SELVBETJENING("selvbetjening");

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
                .or(() -> hentClaim(ISSUER_ISSO.issuerName, "NAVident").map(sub -> new BrukerOgIssuer(ISSUER_ISSO, sub)));
    }

    public String hentInnloggetSystem() {
        return hentClaim(ISSUER_SYSTEM, "sub")
                .orElseThrow(() -> new TilgangskontrollException("Innlogget bruker er ikke systembruker."));
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(claims -> claims.getClaimSet());
    }

}
