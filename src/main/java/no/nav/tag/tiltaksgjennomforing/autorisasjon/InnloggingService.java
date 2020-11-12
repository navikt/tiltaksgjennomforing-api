package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.RequiredArgsConstructor;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
            Set<AltinnReportee> altinnOrganisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent()));
            Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(new Fnr(brukerOgIssuer.getBrukerIdent()));
            return new InnloggetArbeidsgiver(new Fnr(brukerOgIssuer.getBrukerIdent()), tilganger, altinnOrganisasjoner);
        } else if (issuer == Issuer.ISSUER_ISSO && avtalerolle == Avtalerolle.VEILEDER) {
            return new InnloggetVeileder(new NavIdent(brukerOgIssuer.getBrukerIdent()), tilgangskontrollService);
        } else {
            throw new TilgangskontrollException("Ugyldig kombinasjon av issuer og rolle.");
        }
    }

    public InnloggetArbeidsgiver hentInnloggetArbeidsgiver() {
        try {
            return (InnloggetArbeidsgiver) hentInnloggetBruker(Avtalerolle.ARBEIDSGIVER);
        } catch (ClassCastException e) {
            throw new TilgangskontrollException("Innlogget bruker er ikke arbeidsgiver.");
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
