package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.ArbeidsgiverTokenStrategyFactory;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.HentArbeidsgiverToken;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class InnloggingService {
    private final SystembrukerProperties systembrukerProperties;
    private final BeslutterAdGruppeProperties beslutterAdGruppeProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final AxsysService axsysService;
    private final SlettemerkeProperties slettemerkeProperties;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final VeilarbArenaClient veilarbArenaClient;
    private final ArbeidsgiverTokenStrategyFactory arbeidsgiverTokenStrategyFactory;

    public Avtalepart hentAvtalepart(Avtalerolle avtalerolle) {
        BrukerOgIssuer brukerOgIssuer = tokenUtils.hentBrukerOgIssuer().orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget."));
        Issuer issuer = brukerOgIssuer.getIssuer();

        if ((issuer == Issuer.ISSUER_SELVBETJENING || issuer == Issuer.ISSUER_TOKENX) && avtalerolle == Avtalerolle.DELTAKER) {
            return new Deltaker(new Fnr(brukerOgIssuer.getBrukerIdent()));
        }

        else if ((issuer == Issuer.ISSUER_SELVBETJENING || issuer == Issuer.ISSUER_TOKENX) && avtalerolle == Avtalerolle.ARBEIDSGIVER) {
            HentArbeidsgiverToken hentArbeidsgiverToken = arbeidsgiverTokenStrategyFactory.create(issuer);

            Set<AltinnReportee> altinnOrganisasjoner = altinnTilgangsstyringService
                    .hentAltinnOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent()), hentArbeidsgiverToken);
            Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(new Fnr(brukerOgIssuer.getBrukerIdent()), hentArbeidsgiverToken);
            return new Arbeidsgiver(new Fnr(brukerOgIssuer.getBrukerIdent()), altinnOrganisasjoner, tilganger, persondataService, norg2Client, veilarbArenaClient);
        }

        else if (issuer == Issuer.ISSUER_ISSO && avtalerolle == Avtalerolle.VEILEDER) {
            NavIdent navIdent = new NavIdent(brukerOgIssuer.getBrukerIdent());
            Set<NavEnhet> navEnheter = hentNavEnheter(navIdent);
            boolean harAdGruppeForBeslutter = tokenUtils.harAdGruppe(beslutterAdGruppeProperties.getId());
            return new Veileder(navIdent, tilgangskontrollService, persondataService, norg2Client, navEnheter, slettemerkeProperties, tilskuddsperiodeConfig, harAdGruppeForBeslutter, veilarbArenaClient);
        } else if (issuer == Issuer.ISSUER_ISSO && avtalerolle == Avtalerolle.BESLUTTER) {
            boolean harAdGruppeForBeslutter = tokenUtils.harAdGruppe(beslutterAdGruppeProperties.getId());
            if (harAdGruppeForBeslutter) {
                return new Beslutter(new NavIdent(brukerOgIssuer.getBrukerIdent()), tilgangskontrollService, axsysService);
            } else {
                throw new FeilkodeException(Feilkode.MANGLER_AD_GRUPPE_BESLUTTER);
            }
        } else {
            log.warn("Ugyldig kombinasjon av issuer={} og rolle={}", issuer, avtalerolle);
            throw new FeilkodeException(Feilkode.UGYLDIG_KOMBINASJON_AV_ISSUER_OG_ROLLE);
        }
    }

    private Set<NavEnhet> hentNavEnheter(NavIdent navIdent) {
        return new HashSet<NavEnhet>(axsysService.hentEnheterNavAnsattHarTilgangTil(navIdent));
    }

    public Veileder hentVeileder() {
        return (Veileder) hentAvtalepart(Avtalerolle.VEILEDER);
    }

    public Arbeidsgiver hentArbeidsgiver() {
        return (Arbeidsgiver) hentAvtalepart(Avtalerolle.ARBEIDSGIVER);
    }

    public InnloggetBruker hentInnloggetBruker(Avtalerolle avtalerolle) {
        return hentAvtalepart(avtalerolle).innloggetBruker();
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

    public Beslutter hentBeslutter() {
        return (Beslutter) hentAvtalepart(Avtalerolle.BESLUTTER);
    }
}
