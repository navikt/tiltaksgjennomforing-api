package no.nav.tag.tiltaksgjennomforing.integrasjon;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac.TilgangskontrollService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenUtils {
    
    final static String ISSUER_ISSO = "isso";
    final static String ISSUER_SELVBETJENING = "selvbetjening";

    private final OIDCRequestContextHolder contextHolder;
    private final FeatureToggleService featureToggleService;
    private final TilgangskontrollService tilgangskontrollService;

    @Autowired
    public TokenUtils(OIDCRequestContextHolder contextHolder, FeatureToggleService featureToggleService, TilgangskontrollService tilgangskontrollService) {
        this.contextHolder = contextHolder;
        this.featureToggleService = featureToggleService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public InnloggetBruker hentInnloggetBruker() {
        if (erInnloggetNavAnsatt()) {
            return hentInnloggetNavAnsatt();
        } else if (erInnloggetSelvbetjeningBruker()) {
            return hentInnloggetSelvbetjeningBruker();
        } else {
            throw new TilgangskontrollException("Bruker er ikke innlogget.");
        }
    }

    public InnloggetSelvbetjeningBruker hentInnloggetSelvbetjeningBruker() {
        String fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
                .orElseThrow(() -> new TilgangskontrollException("Finner ikke fodselsnummer til bruker."));
        return new InnloggetSelvbetjeningBruker(new Fnr(fnr));
    }

    public InnloggetNavAnsatt hentInnloggetNavAnsatt() {
        String navIdent = hentClaim(ISSUER_ISSO, "NAVident")
                .orElseThrow(() -> new TilgangskontrollException("Innlogget bruker er ikke veileder."));
        return new InnloggetNavAnsatt(new NavIdent(navIdent), featureToggleService, tilgangskontrollService);
    }

    private Optional<String> hentClaim(String issuer, String claim) {
        Optional<JWTClaimsSet> claimSet = hentClaimSet(issuer);
        return claimSet.map(jwtClaimsSet -> String.valueOf(jwtClaimsSet.getClaim(claim)));
    }

    private Optional<JWTClaimsSet> hentClaimSet(String issuer) {
        return Optional.ofNullable(contextHolder.getOIDCValidationContext().getClaims(issuer))
                .map(claims -> claims.getClaimSet());
    }

    public boolean erInnloggetNavAnsatt() {
        return hentClaimSet(ISSUER_ISSO)
                .map(jwtClaimsSet -> (String) jwtClaimsSet.getClaims().get("NAVident"))
                .map(navIdentString -> NavIdent.erGyldigNavIdent(navIdentString))
                .orElse(false);
    }

    public boolean erInnloggetSelvbetjeningBruker() {
        return hentClaim(ISSUER_SELVBETJENING, "sub")
                .map(fnrString -> Fnr.erGyldigFnr(fnrString))
                .orElse(false);
    }
}
