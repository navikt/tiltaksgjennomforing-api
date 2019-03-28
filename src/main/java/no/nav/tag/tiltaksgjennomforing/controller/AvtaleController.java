package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.PilotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
@Transactional
@Timed
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final TokenUtils tokenUtils;
    private final PilotProperties tilgangUnderPilotering;

    @Autowired
    public AvtaleController(AvtaleRepository avtaleRepository, TokenUtils tokenUtils, PilotProperties tilgangUnderPilotering) {
        this.avtaleRepository = avtaleRepository;
        this.tokenUtils = tokenUtils;
        this.tilgangUnderPilotering = tilgangUnderPilotering;
    }

    @GetMapping("/{avtaleId}")
    public ResponseEntity<Avtale> hent(@PathVariable("avtaleId") UUID id) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        avtale.sjekkLesetilgang(innloggetBruker);
        return ResponseEntity.ok(avtale);
    }

    @GetMapping
    public Iterable<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil() {
        InnloggetBruker bruker = tokenUtils.hentInnloggetBruker();
        List<Avtale> avtaler = new ArrayList<>();
        for (Avtale avtale : avtaleRepository.findAll()) {
            if (avtale.harLesetilgang(bruker)) {
                avtaler.add(avtale);
            }
        }
        return avtaler;
    }

    @PostMapping
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        InnloggetNavAnsatt innloggetNavAnsatt = tokenUtils.hentInnloggetNavAnsatt();
        tilgangUnderPilotering.sjekkTilgang(innloggetNavAnsatt.getIdentifikator());
        Avtale avtale = innloggetNavAnsatt.opprettAvtale(opprettAvtale);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                      @RequestHeader("If-Match") Integer versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtalepart avtalepart = avtale.hentAvtalepart(innloggetBruker);
        avtalepart.endreAvtale(versjon, endreAvtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon().toString()).build();
    }

    @GetMapping(value = "/{avtaleId}/rolle")
    public ResponseEntity<Avtalerolle> hentRolle(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtalepart avtalepart = avtale.hentAvtalepart(innloggetBruker);
        return ResponseEntity.ok(avtalepart.rolle());
    }

    @PostMapping(value = "/{avtaleId}/opphev-godkjenninger")
    public ResponseEntity opphevGodkjenninger(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtalepart avtalepart = avtale.hentAvtalepart(innloggetBruker);
        avtalepart.opphevGodkjenninger();
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/godkjenn")
    public ResponseEntity godkjenn(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtalepart avtalepart = avtale.hentAvtalepart(innloggetBruker);
        avtalepart.godkjennAvtale();
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }
}
