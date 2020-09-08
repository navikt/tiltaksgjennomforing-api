package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InnloggingService {

    private final SystembrukerProperties systembrukerProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;
    private final TilgangskontrollService tilgangskontrollService;

    public InnloggetBruker<? extends Identifikator> hentInnloggetBruker(Avtalerolle avtalerolle) {
        BrukerOgIssuer brukerOgIssuer = tokenUtils.hentBrukerOgIssuer().orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget."));
        Issuer issuer = brukerOgIssuer.getIssuer();
        if (issuer == Issuer.ISSUER_SELVBETJENING && avtalerolle == Avtalerolle.DELTAKER) {
            return new InnloggetDeltaker(new Fnr(brukerOgIssuer.getBrukerIdent()));
        } else if (issuer == Issuer.ISSUER_SELVBETJENING && avtalerolle == Avtalerolle.ARBEIDSGIVER) {
            List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent()));
            return new InnloggetArbeidsgiver(new Fnr(brukerOgIssuer.getBrukerIdent()), organisasjoner);
        } else if (issuer == Issuer.ISSUER_ISSO && avtalerolle == Avtalerolle.VEILEDER) {
            return new InnloggetVeileder(new NavIdent(brukerOgIssuer.getBrukerIdent()), tilgangskontrollService);
        } else {
            throw new TilgangskontrollException("Ugyldig kombinasjon av issuer og rolle.");
        }
    }

    public InnloggetVeileder hentInnloggetVeileder() {
        try {
            return (InnloggetVeileder) hentInnloggetBruker(Avtalerolle.VEILEDER);
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
