package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.integrasjon.altinn_tilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.SystembrukerProperties;
import no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac.TilgangskontrollService;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InnloggingService {

    private final SystembrukerProperties systembrukerProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;
    private final TilgangskontrollService tilgangskontrollService;

    public InnloggetBruker hentInnloggetBruker() {
        BrukerOgIssuer brukerOgIssuer = tokenUtils.hentBrukerOgIssuer().orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget."));
        return Issuer.ISSUER_SELVBETJENING == brukerOgIssuer.getIssuer()
                ? new InnloggetSelvbetjeningBruker(new Fnr(brukerOgIssuer.getBrukerIdent()), altinnTilgangsstyringService.hentOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent()))) 
                : new InnloggetNavAnsatt(new NavIdent(brukerOgIssuer.getBrukerIdent()), tilgangskontrollService);
    }

    public InnloggetNavAnsatt hentInnloggetNavAnsatt() {
        try {
            return (InnloggetNavAnsatt) hentInnloggetBruker();
        } catch (ClassCastException e) {
            throw new TilgangskontrollException("Innlogget bruker er ikke veileder.");
        }
    }

    public void validerSystembruker() {
        tokenUtils.hentBrukerOgIssuer()
            .filter(t -> (Issuer.ISSUER_SYSTEM == t.getIssuer() && systembrukerProperties.getId().equals(t.getBrukerIdent())))
            .orElseThrow(() -> new TilgangskontrollException("Systemet har ikke tilgang til tjenesten"));
    }

}
