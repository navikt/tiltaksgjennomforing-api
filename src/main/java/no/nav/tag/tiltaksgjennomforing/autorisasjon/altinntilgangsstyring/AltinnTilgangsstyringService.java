package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final AltinnTilgangsstyringKlient klient;

    public AltinnTilgangsstyringService(
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
        AltinnTilgangsstyringKlient klient
    ) {

        if (Utils.erNoenTomme(
            altinnTilgangsstyringProperties.getArbtreningServiceCode(),
            altinnTilgangsstyringProperties.getArbtreningServiceEdition(),
            altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
            altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(),
            altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
            altinnTilgangsstyringProperties.getLtsVarigServiceEdition(),
            altinnTilgangsstyringProperties.getSommerjobbServiceCode(),
            altinnTilgangsstyringProperties.getSommerjobbServiceEdition(),
            altinnTilgangsstyringProperties.getVtaoServiceCode(),
            altinnTilgangsstyringProperties.getVtaoServiceEdition(),
            altinnTilgangsstyringProperties.getArbeidsgiverAltinnTilgangerUri()
        )) {
            throw new TiltaksgjennomforingException("Altinn konfigurasjon ikke komplett");
        }
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.klient = klient;
    }

    public AltinnTilgangerDto hentAltinnTilganger(Fnr fnr) {
        AltinnTilgangerResponse response = klient.kallAltinn3(fnr);

        return new AltinnTilgangerDto(
            response.hierarki(),
            mapTilgangerFraAltinn3(response),
            mapAdressesperreFraAltinn3(response)
        );
    }

    private List<BedriftNr> mapAdressesperreFraAltinn3(AltinnTilgangerResponse response) {
        if (response.tilgangTilOrgNr() == null) {
            return List.of();
        }
        Set<String> orgNrs = new HashSet<>();

        // Altinn 3 ressurs
        Set<String> altinn3 = response.tilgangTilOrgNr().get(AltinnTilgangsstyringProperties.ADRESSESPERRE);
        if (altinn3 != null) {
            orgNrs.addAll(altinn3);
        }

        // Altinn 2 serviceCode:serviceEdition
        // Kan fjernes på sikt (juni 26) - letter overgangen til Altinn 3
        String altinn2Tilgang = altinnTilgangsstyringProperties.getAdressesperreServiceCode()
            + ":" + altinnTilgangsstyringProperties.getAdressesperreServiceEdition();
        Set<String> altinn2 = response.tilgangTilOrgNr().get(altinn2Tilgang);
        if (altinn2 != null) {
            orgNrs.addAll(altinn2);
        }

        return orgNrs.stream().map(BedriftNr::new).toList();
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
}
