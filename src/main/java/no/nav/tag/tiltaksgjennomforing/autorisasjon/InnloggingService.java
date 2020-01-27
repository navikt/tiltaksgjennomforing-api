package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InnloggingService {

    private final SystembrukerProperties systembrukerProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;
    private final TilgangskontrollService tilgangskontrollService;

    public InnloggetBruker<? extends Identifikator> hentInnloggetBruker(Optional<Avtalerolle> avtalerolle) {
        BrukerOgIssuer brukerOgIssuer = tokenUtils.hentBrukerOgIssuer().orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget."));
        boolean erDeltaker = (avtalerolle.isPresent() && avtalerolle.get().equals(Avtalerolle.DELTAKER));
        return Issuer.ISSUER_SELVBETJENING == brukerOgIssuer.getIssuer()
                ? new InnloggetSelvbetjeningBruker(new Fnr(brukerOgIssuer.getBrukerIdent()), erDeltaker ? Collections.emptyList() : altinnTilgangsstyringService.hentOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent())))
                : new InnloggetNavAnsatt(new NavIdent(brukerOgIssuer.getBrukerIdent()), tilgangskontrollService);
    }

    public InnloggetBruker<? extends Identifikator> hentInnloggetBruker() {
        return hentInnloggetBruker(Optional.empty());
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
