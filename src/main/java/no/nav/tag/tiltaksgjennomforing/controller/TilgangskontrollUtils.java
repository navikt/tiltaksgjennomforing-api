package no.nav.tag.tiltaksgjennomforing.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TilgangskontrollUtils {
    private final OIDCRequestContextHolder contextHolder;
    private final static String ISSUER_ISSO = "isso";
    private final static String ISSUER_SELVBETJENING = "selvbetjening";

    @Autowired
    public TilgangskontrollUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public Person hentInnloggetPerson() {
        if (innloggetPersonErVeileder()) {
            return hentInnloggetVeileder();
        } else {
            return new Bruker(getClaim(ISSUER_SELVBETJENING, "sub"));
        }
    }

    public Veileder hentInnloggetVeileder() {
        if (innloggetPersonErVeileder()) {
            return new Veileder(getClaim(ISSUER_ISSO, "NAVident"));
        }
        throw new TilgangskontrollException("Innlogget bruker er ikke veileder.");
    }

    private String getClaim(String issuer, String claim) {
        return String.valueOf(getClaimSet(issuer).getClaim(claim));
    }

    private JWTClaimsSet getClaimSet(String issuer) {
        return contextHolder
                .getOIDCValidationContext()
                .getClaims(issuer)
                .getClaimSet();
    }

    private boolean innloggetPersonErVeileder() {
        return getClaimSet(ISSUER_ISSO)
                .getClaims()
                .containsKey("NAVident");
    }
}
