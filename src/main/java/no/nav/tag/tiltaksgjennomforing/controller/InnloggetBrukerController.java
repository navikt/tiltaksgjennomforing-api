package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@RequestMapping("/innlogget-bruker")
@Transactional
public class InnloggetBrukerController {
    private final TokenUtils tilgangskontroll;

    @Autowired
    public InnloggetBrukerController(TokenUtils tilgangskontroll) {
        this.tilgangskontroll = tilgangskontroll;
    }

    @GetMapping
    public ResponseEntity<InnloggetBruker> hentInnloggetBruker() {
        return ResponseEntity.ok(tilgangskontroll.hentInnloggetBruker());
    }
}
