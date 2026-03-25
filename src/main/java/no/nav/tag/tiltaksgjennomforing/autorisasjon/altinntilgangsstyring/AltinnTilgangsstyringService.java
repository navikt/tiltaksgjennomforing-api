package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientFallbackException;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.utils.MultiValueMap;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final AltinnrettigheterProxyKlient klient;
    private final RestTemplate azureRestTemplate;

    public AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            TokenUtils tokenUtils,
            RestTemplate azureRestTemplate,
            @Value("${spring.application.name}") String applicationName) {

        if (Utils.erNoenTomme(altinnTilgangsstyringProperties.getArbtreningServiceCode(),
                altinnTilgangsstyringProperties.getArbtreningServiceEdition(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(),
                altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                altinnTilgangsstyringProperties.getLtsVarigServiceEdition(),
                altinnTilgangsstyringProperties.getSommerjobbServiceCode(),
                altinnTilgangsstyringProperties.getSommerjobbServiceEdition(),
                altinnTilgangsstyringProperties.getVtaoServiceCode(),
                altinnTilgangsstyringProperties.getVtaoServiceEdition(),
                altinnTilgangsstyringProperties.getArbeidsgiverAltinnTilgangerUri())) {
            throw new TiltaksgjennomforingException("Altinn konfigurasjon ikke komplett");
        }
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.azureRestTemplate = azureRestTemplate;

        String altinnProxyUrl = altinnTilgangsstyringProperties.getProxyUri().toString();
        String altinnProxyFallbackUrl = altinnTilgangsstyringProperties.getUri().toString();

        AltinnrettigheterProxyKlientConfig proxyKlientConfig = new AltinnrettigheterProxyKlientConfig(
                new ProxyConfig(applicationName, altinnProxyUrl),
                new AltinnConfig(
                        altinnProxyFallbackUrl,
                        altinnTilgangsstyringProperties.getAltinnApiKey(),
                        altinnTilgangsstyringProperties.getApiGwApiKey()
                )
        );
        this.klient = new AltinnrettigheterProxyKlient(proxyKlientConfig);
    }

    public List<BedriftNr> hentAdressesperreTilganger(Fnr fnr, HentArbeidsgiverToken hentArbeidsgiverToken) {
        String arbeidsgiverToken = hentArbeidsgiverToken.hentArbeidsgiverToken();
        AltinnReportee[] adressesperreTilganger = kallAltinn(altinnTilgangsstyringProperties.getAdressesperreServiceCode(), altinnTilgangsstyringProperties.getAdressesperreServiceEdition(), fnr, arbeidsgiverToken);
        List<BedriftNr> bedrifter = Arrays.stream(adressesperreTilganger)
                .filter(altinnReportee -> !altinnReportee.getType().equals("Enterprise"))
                .map(altinnReportee -> new BedriftNr(altinnReportee.getOrganizationNumber()))
                .toList();
        return bedrifter;
    }

    public Map<BedriftNr, Collection<Tiltakstype>> hentTilganger(Fnr fnr, HentArbeidsgiverToken hentArbeidsgiverToken) {
        MultiValueMap<BedriftNr, Tiltakstype> tilganger = MultiValueMap.empty();
        String arbeidsgiverToken = hentArbeidsgiverToken.hentArbeidsgiverToken();

        AltinnReportee[] arbeidstreningOrger = kallAltinn(altinnTilgangsstyringProperties.getArbtreningServiceCode(), altinnTilgangsstyringProperties.getArbtreningServiceEdition(), fnr, arbeidsgiverToken);
        leggTil(tilganger, arbeidstreningOrger, Tiltakstype.ARBEIDSTRENING);

        AltinnReportee[] varigLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsVarigServiceCode(), altinnTilgangsstyringProperties.getLtsVarigServiceEdition(), fnr, arbeidsgiverToken);
        leggTil(tilganger, varigLtsOrger, Tiltakstype.VARIG_LONNSTILSKUDD);

        AltinnReportee[] midlLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(), altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(), fnr, arbeidsgiverToken);
        leggTil(tilganger, midlLtsOrger, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        AltinnReportee[] sommerjobbOrger = kallAltinn(altinnTilgangsstyringProperties.getSommerjobbServiceCode(), altinnTilgangsstyringProperties.getSommerjobbServiceEdition(), fnr, arbeidsgiverToken);
        leggTil(tilganger, sommerjobbOrger, Tiltakstype.SOMMERJOBB);

        AltinnReportee[] mentorOrger = kallAltinn(altinnTilgangsstyringProperties.getMentorServiceCode(), altinnTilgangsstyringProperties.getMentorServiceEdition(), fnr, arbeidsgiverToken);
        leggTil(tilganger, mentorOrger, Tiltakstype.MENTOR);

        AltinnReportee[] inkluderingstilskuddOrger = kallAltinn(altinnTilgangsstyringProperties.getInkluderingstilskuddServiceCode(), altinnTilgangsstyringProperties.getInkluderingstilskuddServiceEdition(), fnr,
                arbeidsgiverToken);
        leggTil(tilganger, inkluderingstilskuddOrger, Tiltakstype.INKLUDERINGSTILSKUDD);

        AltinnReportee[] vtaoOrger = kallAltinn(altinnTilgangsstyringProperties.getVtaoServiceCode(), altinnTilgangsstyringProperties.getVtaoServiceEdition(), fnr,
                arbeidsgiverToken);
        leggTil(tilganger, vtaoOrger, Tiltakstype.VTAO);


        return tilganger.toMap();
    }

    private void leggTil(MultiValueMap<BedriftNr, Tiltakstype> tilganger, AltinnReportee[] arbeidstreningOrger, Tiltakstype tiltakstype) {
        for (AltinnReportee altinnReportee : arbeidstreningOrger) {
            if (!altinnReportee.getType().equals("Enterprise")) {
                tilganger.put(new BedriftNr(altinnReportee.getOrganizationNumber()), tiltakstype);
            }
        }
    }

    public Set<AltinnReportee> hentAltinnOrganisasjoner(Fnr fnr, HentArbeidsgiverToken hentArbeidsgiverToken) {
        return new HashSet<>(List.of(kallAltinn(null, null, fnr, hentArbeidsgiverToken.hentArbeidsgiverToken())));
    }

    private AltinnReportee[] kallAltinn(Integer serviceCode, Integer serviceEdition, Fnr fnr, String arbeidsgiverToken) {
        try {
            List<AltinnReportee> reportees;
            if (serviceCode != null && serviceEdition != null) {
                reportees = klient.hentOrganisasjoner(
                        new SelvbetjeningToken(arbeidsgiverToken),
                        new Subject(fnr.asString()), new ServiceCode(serviceCode.toString()), new ServiceEdition(serviceEdition.toString()),
                        true
                );
            } else {
                reportees = klient.hentOrganisasjoner(new SelvbetjeningToken(arbeidsgiverToken), new Subject(fnr.asString()), true);
            }
            return reportees.toArray(new AltinnReportee[0]);

        } catch (AltinnrettigheterProxyKlientFallbackException exception) {
            log.warn("Feil ved kall mot Altinn.", exception);
            throw new AltinnFeilException();
        }
    }

    public AltinnTilgangerDto hentAltinnTilganger() {
        var request = new AltinnTilgangerRequest(new AltinnTilgangerFilter(Set.of(), Set.of()));
        AltinnTilgangerResponse response = kallAltinn3(request);

        return new AltinnTilgangerDto(
            response.hierarki(),
            mapTilgangerFraAltinn3(response)
        );
    }
    
    private Map<BedriftNr, Set<Tiltakstype>> mapTilgangerFraAltinn3(AltinnTilgangerResponse response) {
        Map<String, Tiltakstype> tilgangerTilTiltakstype = altinnTilgangsstyringProperties.tilgangerTilTiltakstype();
        Map<BedriftNr, Set<Tiltakstype>> tilganger = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : response.orgNrTilTilganger().entrySet()) {
            BedriftNr bedriftNr = new BedriftNr(entry.getKey());
            for (String tilgang : entry.getValue()) {
                Tiltakstype tiltakstype = tilgangerTilTiltakstype.get(tilgang);
                if (tiltakstype != null) {
                    tilganger.computeIfAbsent(bedriftNr, k -> new HashSet<>()).add(tiltakstype);
                }
            }
        }

        return tilganger;
    }

    private AltinnTilgangerResponse kallAltinn3(AltinnTilgangerRequest altinnTilgangerRequest) {
        AltinnTilgangerResponse response;
        try {
            response = azureRestTemplate.postForObject(
                    altinnTilgangsstyringProperties.getArbeidsgiverAltinnTilgangerUri(),
                    altinnTilgangerRequest,
                    AltinnTilgangerResponse.class
            );
        } catch (RestClientResponseException e) {
            log.error("HTTP-feil fra arbeidsgiver-altinn-tilganger: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AltinnFeilException();
        } catch (ResourceAccessException e) {
            log.error("Nettverksfeil ved kall til arbeidsgiver-altinn-tilganger", e);
            throw new AltinnFeilException();
        }

        if (response == null || response.isError() || response.orgNrTilTilganger() == null) {
            log.warn("Ugyldig respons fra arbeidsgiver-altinn-tilganger, isError: {}", response != null ? response.isError() : "null");
            throw new AltinnFeilException();
        }

        return response;
    }
}
