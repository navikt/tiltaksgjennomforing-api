package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final TilgangskontrollUtils tilgangskontroll;

    @Autowired
    public AvtaleController(AvtaleRepository avtaleRepository, TilgangskontrollUtils tilgangskontroll) {
        this.avtaleRepository = avtaleRepository;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avtale> hent(@PathVariable("id") Integer id) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (avtale.erTilgjengeligFor(tilgangskontroll.hentInnloggetPerson())) {
            return ResponseEntity.ok(avtale);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    public Iterable<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil() {
        Person bruker = tilgangskontroll.hentInnloggetPerson();
        List<Avtale> avtaler = new ArrayList<>();
        for (Avtale avtale : avtaleRepository.findAll()) {
            if (avtale.erTilgjengeligFor(bruker)) {
                avtaler.add(avtale);
            }
        }
        return avtaler;
    }

    @PostMapping
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        Avtale avtale = tilgangskontroll.hentInnloggetVeileder().opprettAvtale(opprettAvtale);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") Integer avtaleId,
                                      @RequestHeader("If-Match") Integer versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        Optional<Avtale> optionalAvtale = avtaleRepository.findById(avtaleId);
        if (optionalAvtale.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Avtale avtale = optionalAvtale.get();
        if (optionalAvtale.get().erTilgjengeligFor(tilgangskontroll.hentInnloggetPerson())) {
            avtale.endreAvtale(versjon, endreAvtale);
            Avtale lagretAvtale = avtaleRepository.save(avtale);
            return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon().toString()).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
