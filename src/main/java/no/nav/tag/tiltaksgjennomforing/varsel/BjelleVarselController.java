package no.nav.tag.tiltaksgjennomforing.varsel;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Protected
@RestController
@RequestMapping("/varsler")
@Timed
@RequiredArgsConstructor
public class BjelleVarselController {
    private final InnloggingService innloggingService;
    private final BjelleVarselService bjelleVarselService;

    @GetMapping
    public Iterable<BjelleVarsel> hentVarsler(
            @RequestParam(value = "avtaleId", required = false) UUID avtaleId,
            @RequestParam(value = "lest", required = false) Boolean lest, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        return bjelleVarselService.varslerForAvtalepart(avtalepart, avtaleId, lest);
    }

    @PostMapping("{varselId}/sett-til-lest")
    @Transactional
    public ResponseEntity<?> settTilLest(@PathVariable("varselId") UUID varselId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        bjelleVarselService.settTilLest(avtalepart, varselId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sett-alle-til-lest")
    @Transactional
    public ResponseEntity<?> settVarslerTilLest(@RequestBody List<UUID> varselIder, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        bjelleVarselService.settVarslerTilLest(avtalepart, varselIder);
        return ResponseEntity.ok().build();
    }
}
