package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.*;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final TokenUtils tokenUtils;
    private final AltinnrettigheterProxyKlient klient;

    @Autowired
    public AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            TokenUtils tokenUtils) {
        if (Utils.erNoenTomme(altinnTilgangsstyringProperties.getArbtreningServiceCode(),
                altinnTilgangsstyringProperties.getArbtreningServiceEdition(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(),
                altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                altinnTilgangsstyringProperties.getLtsVarigServiceEdition())) {
            throw new TiltaksgjennomforingException("Altinn konfigurasjon ikke komplett");
        }
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.tokenUtils = tokenUtils;


        String altinnProxyUrl = altinnTilgangsstyringProperties.getProxyUri().toString();
        String altinnProxyFallbackUrl = altinnTilgangsstyringProperties.getUri().toString();

        AltinnrettigheterProxyKlientConfig proxyKlientConfig = new AltinnrettigheterProxyKlientConfig(
                new ProxyConfig("tiltaksgjennomforing-api", altinnProxyUrl),
                new no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig(
                        altinnProxyFallbackUrl,
                        altinnTilgangsstyringProperties.getAltinnApiKey(),
                        altinnTilgangsstyringProperties.getApiGwApiKey()
                )
        );
        this.klient = new AltinnrettigheterProxyKlient(proxyKlientConfig);


    }

    public Map<BedriftNr, Collection<Tiltakstype>> hentTilganger(Fnr fnr) {
        Multimap<BedriftNr, Tiltakstype> tilganger = HashMultimap.create();

        AltinnReportee[] arbeidstreningOrger = kallAltinn(altinnTilgangsstyringProperties.getArbtreningServiceCode(), altinnTilgangsstyringProperties.getArbtreningServiceEdition(), fnr);
        leggTil(tilganger, arbeidstreningOrger, Tiltakstype.ARBEIDSTRENING);

        AltinnReportee[] varigLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsVarigServiceCode(), altinnTilgangsstyringProperties.getLtsVarigServiceEdition(), fnr);
        leggTil(tilganger, varigLtsOrger, Tiltakstype.VARIG_LONNSTILSKUDD);

        AltinnReportee[] midlLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(), altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(), fnr);
        leggTil(tilganger, midlLtsOrger, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        return tilganger.asMap();
    }

    private void leggTil(Multimap<BedriftNr, Tiltakstype> tilganger, AltinnReportee[] arbeidstreningOrger, Tiltakstype tiltakstype) {
        for (AltinnReportee altinnReportee : arbeidstreningOrger) {
            if (!altinnReportee.getType().equals("Enterprise")) {
                tilganger.put(new BedriftNr(altinnReportee.getOrganizationNumber()), tiltakstype);
            }
        }
    }

    public Set<AltinnReportee> hentAltinnOrganisasjoner(Fnr fnr) {
        return new HashSet<>(List.of(kallAltinn(null, null, fnr)));
    }

    private AltinnReportee[] kallAltinn(Integer serviceCode, Integer serviceEdition, Fnr fnr) {
        try {
                List<AltinnReportee> reportees;
                if (serviceCode != null && serviceEdition != null) {
                    reportees = klient.hentOrganisasjoner(
                            new SelvbetjeningToken(tokenUtils.hentSelvbetjeningToken()),
                            new Subject(fnr.asString()), new ServiceCode(serviceCode.toString()), new ServiceEdition(serviceEdition.toString()),
                            true
                    );
                } else {
                    reportees = klient.hentOrganisasjoner(new SelvbetjeningToken(tokenUtils.hentSelvbetjeningToken()), new Subject(fnr.asString()), true);
                }
                return reportees.toArray(new AltinnReportee[0]);

        } catch (RestClientException exception) {
            log.warn("Feil ved kall mot Altinn.", exception);
            throw new AltinnFeilException();
        }
    }
}
