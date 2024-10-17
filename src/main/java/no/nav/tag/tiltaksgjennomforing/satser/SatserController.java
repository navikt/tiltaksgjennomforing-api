package no.nav.tag.tiltaksgjennomforing.satser;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/satser")
class SatserController {
    private final SatsService satsService;

    SatserController(SatsService satsService) {
        this.satsService = satsService;
    }

    @Unprotected
    @GetMapping("/sats/{satsType}")
    Sats hentSatser(@PathVariable("satsType") String satsType) {
        return satsService.hentSats(satsType.toLowerCase());
    }

    @ProtectedWithClaims(issuer = "aad")
    @PostMapping("/sats/{satsType}")
    ResponseEntity<Void> settInnSats(@PathVariable("satsType") String satsType, @RequestBody SatsPeriodeData satsPeriodeData) {
        satsService.opprettNySats(satsType.toLowerCase(), satsPeriodeData);
        return ResponseEntity.status(201).body(null);
    }

    @Unprotected
    @GetMapping("/typer")
    public Set<String> satseTyper() {
        return satsService.hentSatsetyper();
    }
}
