package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@RequestMapping("/innlogget-bruker")
public class InnloggetBrukerController {
    private final InnloggingService innloggingService;

    @Autowired
    public InnloggetBrukerController(InnloggingService innloggingService) {
        this.innloggingService = innloggingService;
    }

    @GetMapping
    public ResponseEntity<InnloggetBruker> hentInnloggetBruker() {
        return ResponseEntity.ok(innloggingService.hentInnloggetBruker());
    }
}
