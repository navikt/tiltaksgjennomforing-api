package no.nav.tag.tiltaksgjennomforing.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TilgangskontrollUtils {
    private final OIDCRequestContextHolder contextHolder;
    private final static String ISSUER = "isso";

    @Autowired
    public TilgangskontrollUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public Person hentInnloggetBruker() {
        if (brukerErVeileder()) {
            return new Veileder(getNavIdentFraToken());
        } else {
            return new Bruker(getFnrFraToken());
        }
    }

    private JWTClaimsSet getClaimSet() {
        return contextHolder
                .getOIDCValidationContext()
                .getClaims(ISSUER)
                .getClaimSet();
    }

    private boolean brukerErVeileder() {
        return getClaimSet()
                .getClaims()
                .containsKey("NAVident");
    }

    private NavIdent getNavIdentFraToken() {
        return new NavIdent(String.valueOf(
                getClaimSet().getClaim("NAVident")
        ));

    }

    private Fnr getFnrFraToken() {
        return new Fnr(String.valueOf(
                getClaimSet().getClaim("sub")
        ));
    }
}
