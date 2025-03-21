package no.nav.tag.tiltaksgjennomforing.varsel;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Protected
@RestController
@RequestMapping("/varsler")
@Timed
@RequiredArgsConstructor
public class VarselController {
    private final InnloggingService innloggingService;
    private final VarselRepository varselRepository;
    private final AvtaleRepository avtaleRepository;

    @GetMapping("/oversikt")
    public List<VarselRespons> hentVarslerMedBjelleForOversikt(
            @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        List<Varsel> varsler = varselRepository.findAllByLestIsFalseAndBjelleIsTrueAndIdentifikatorIn(avtalepart.identifikatorer());
        return varsler.stream().map(varsel -> new VarselRespons(varsel, innloggetPart)).toList();
    }

    @GetMapping("/avtale-modal")
    public List<VarselRespons> hentVarslerMedBjelleForAvtale(
            @RequestParam(value = "avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        List<Varsel> varsler = varselRepository.findAllByLestIsFalseAndBjelleIsTrueAndAvtaleIdAndIdentifikatorIn(avtaleId, avtalepart.identifikatorer());
        return varsler.stream().map(varsel -> new VarselRespons(varsel, innloggetPart)).toList();    }

    @GetMapping("/avtale-logg")
    public List<VarselRespons> hentAlleVarslerForAvtale(
            @RequestParam(value = "avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow();
        avtalepart.sjekkTilgang(avtale);
        List<Varsel> varsler = varselRepository.findAllByAvtaleIdAndMottaker(avtaleId, innloggetPart);
        return varsler.stream().map(varsel -> new VarselRespons(varsel, innloggetPart)).toList();    }

    @PostMapping("{varselId}/sett-til-lest")
    @Transactional
    public ResponseEntity<?> settTilLest(@PathVariable("varselId") UUID varselId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Varsel varsel = varselRepository.findByIdAndIdentifikatorIn(varselId, avtalepart.identifikatorer());
        varsel.settTilLest();
        varselRepository.save(varsel);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sett-alle-til-lest")
    @Transactional
    public ResponseEntity<?> settFlereVarslerTilLest(@RequestBody List<UUID> varselIder, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        varselIder.forEach(varselId -> settTilLest(varselId, innloggetPart));
        return ResponseEntity.ok().build();
    }
}
