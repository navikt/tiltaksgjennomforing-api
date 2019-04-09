package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.integrasjon.BrregService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@RequestMapping("/organisasjon")
@Timed
@RequiredArgsConstructor
public class OrganisasjonController {
    private final BrregService brregService;

    @GetMapping("/{bedriftNr}")
    public Organisasjon hentOrganisasjon(@PathVariable("bedriftNr") BedriftNr bedriftNr) {
        return brregService.hentOrganisasjon(bedriftNr);
    }
}
