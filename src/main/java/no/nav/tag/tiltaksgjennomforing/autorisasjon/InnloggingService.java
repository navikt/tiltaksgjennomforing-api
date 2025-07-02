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
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Beslutter;
import no.nav.tag.tiltaksgjennomforing.avtale.Deltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Mentor;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.RolleHarIkkeTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class InnloggingService {
    private final SystembrukerProperties systembrukerProperties;
    private final AdGruppeProperties adGrupperProperties;
    private final TokenUtils tokenUtils;
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final AxsysService axsysService;
    private final SlettemerkeProperties slettemerkeProperties;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final ArbeidsgiverTokenStrategyFactory arbeidsgiverTokenStrategyFactory;
    private final FeatureToggleService featureToggleService;
    private final EregService eregService;

    public Avtalepart hentAvtalepart(Avtalerolle avtalerolle) {
        BrukerOgIssuer brukerOgIssuer = tokenUtils.hentBrukerOgIssuer().orElseThrow(() -> new TilgangskontrollException("Bruker er ikke innlogget."));
        Issuer issuer = brukerOgIssuer.getIssuer();

        if (issuer == Issuer.ISSUER_TOKENX && avtalerolle == Avtalerolle.DELTAKER) {
            return new Deltaker(new Fnr(brukerOgIssuer.getBrukerIdent()));
        }
        if (issuer == Issuer.ISSUER_TOKENX && avtalerolle == Avtalerolle.MENTOR) {
            return new Mentor(new Fnr(brukerOgIssuer.getBrukerIdent()));
        }
        if (issuer == Issuer.ISSUER_TOKENX && avtalerolle == Avtalerolle.ARBEIDSGIVER) {
            HentArbeidsgiverToken hentArbeidsgiverToken = arbeidsgiverTokenStrategyFactory.create(issuer);
            Set<AltinnReportee> altinnOrganisasjoner = altinnTilgangsstyringService
                    .hentAltinnOrganisasjoner(new Fnr(brukerOgIssuer.getBrukerIdent()), hentArbeidsgiverToken);
            Map<BedriftNr, Collection<Tiltakstype>> tilganger = altinnTilgangsstyringService.hentTilganger(
                new Fnr(brukerOgIssuer.getBrukerIdent()), hentArbeidsgiverToken);
            List<BedriftNr> adressesperreTilganger = altinnTilgangsstyringService.hentAdressesperreTilganger(new Fnr(brukerOgIssuer.getBrukerIdent()), hentArbeidsgiverToken);
            return new Arbeidsgiver(
                new Fnr(brukerOgIssuer.getBrukerIdent()),
                altinnOrganisasjoner,
                tilganger,
                adressesperreTilganger,
                persondataService,
                norg2Client,
                eregService
            );
        }
        if (issuer == Issuer.ISSUER_AAD && avtalerolle == Avtalerolle.VEILEDER) {
            NavIdent navIdent = new NavIdent(brukerOgIssuer.getBrukerIdent());
            Set<NavEnhet> navEnheter = hentNavEnheter(navIdent);
            AdGruppeTilganger adGruppeTilganger = AdGruppeTilganger.av(adGrupperProperties, tokenUtils);
            return new Veileder(
                navIdent,
                tokenUtils.hentAzureOid(),
                tilgangskontrollService,
                persondataService,
                norg2Client,
                navEnheter,
                slettemerkeProperties,
                adGruppeTilganger,
                veilarboppfolgingService,
                featureToggleService,
                eregService
            );
        }
        if (issuer == Issuer.ISSUER_AAD && avtalerolle == Avtalerolle.BESLUTTER) {
            AdGruppeTilganger adGruppeTilganger = AdGruppeTilganger.av(adGrupperProperties, tokenUtils);

            if (!adGruppeTilganger.beslutter()) {
                log.warn("Ugyldig kombinasjon av issuer={} og rolle={}", issuer, avtalerolle);
                throw new FeilkodeException(Feilkode.UGYLDIG_KOMBINASJON_AV_ISSUER_OG_ROLLE);
            }

            var navIdent = new NavIdent(brukerOgIssuer.getBrukerIdent());
            var navEnheter = hentNavEnheter(navIdent);
            return new Beslutter(
                navIdent,
                tokenUtils.hentAzureOid(),
                navEnheter,
                tilgangskontrollService,
                norg2Client,
                persondataService,
                adGruppeTilganger
            );
        }

        log.warn("Ugyldig kombinasjon av issuer={} og rolle={}", issuer, avtalerolle);
        throw new FeilkodeException(Feilkode.UGYLDIG_KOMBINASJON_AV_ISSUER_OG_ROLLE);
    }

    private Set<NavEnhet> hentNavEnheter(NavIdent navIdent) {
        return new HashSet<>(axsysService.hentEnheterNavAnsattHarTilgangTil(navIdent));
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
            throw new RolleHarIkkeTilgangException();
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
