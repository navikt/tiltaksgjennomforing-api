package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.BjelleVarselService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@RequestMapping("/varsel")
@Timed
@RequiredArgsConstructor
public class BjelleVarselController {
    private final InnloggingService innloggingService;
    private final BjelleVarselService bjelleVarselService;

    @GetMapping
    public Iterable<BjelleVarsel> hentAlleVarsler() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        return bjelleVarselService.mineBjelleVarsler(bruker);
    }

    @GetMapping("uleste")
    public Iterable<BjelleVarsel> hentAlleUlesteVarsler() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        return bjelleVarselService.mineUlesteBjelleVarsler(bruker);
    }

    @PostMapping("sett-til-lest")
    @Transactional
    public ResponseEntity settTilLest() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        bjelleVarselService.settTilLest(bruker);
        return ResponseEntity.ok().build();
    }
}
