package no.nav.tag.tiltaksgjennomforing.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtils {
    private final OIDCRequestContextHolder contextHolder;

    @Autowired
    public AuthorizationUtils(OIDCRequestContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    public boolean harTilgangTilAvtale(Avtale avtale) {
        if (brukerErVeileder()) {
            return getNavIdentFraToken().equals(avtale.getVeilederNavIdent());
        } else {
            Fnr fnr = getFnrFraToken();
            return fnr.equals(avtale.getDeltakerFnr()) || fnr.equals(avtale.getArbeidsgiverFnr());
        }
    }

    private JWTClaimsSet getClaimSet() {
        return contextHolder
                .getOIDCValidationContext()
                .getClaims("localhost")
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
