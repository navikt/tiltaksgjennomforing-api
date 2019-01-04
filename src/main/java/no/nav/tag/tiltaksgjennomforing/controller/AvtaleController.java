package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.domene.OpprettAvtale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static no.nav.tag.tiltaksgjennomforing.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final TilgangskontrollUtils tilgangskontrollUtils;

    @Autowired
    public AvtaleController(AvtaleRepository avtaleRepository, TilgangskontrollUtils tilgangskontrollUtils) {
        this.avtaleRepository = avtaleRepository;
        this.tilgangskontrollUtils = tilgangskontrollUtils;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avtale> hent(@PathVariable("id") Integer id) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (avtale.erTilgjengeligFor(tilgangskontrollUtils.hentPersonFraToken())) {
            return ResponseEntity.ok(avtale);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    public Iterable<Avtale> hentAlle() {
        return avtaleRepository.findAll();
    }

    @PostMapping
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        Avtale opprettetAvtale = avtaleRepository.save(Avtale.nyAvtale(opprettAvtale));
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") Integer avtaleId,
                                      @RequestHeader("If-Match") String versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        return avtaleRepository.findById(avtaleId)
                .map(avtale -> {
                    avtale.sjekkVersjon(versjon);
                    avtale.endreAvtale(endreAvtale);
                    Avtale lagretAvtale = avtaleRepository.save(avtale);
                    return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon()).build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
