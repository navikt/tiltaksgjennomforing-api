package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.domene.OpprettAvtale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static no.nav.tag.tiltaksgjennomforing.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;

    @Autowired
    public AvtaleController(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avtale> hent(@PathVariable("id") Integer id) {
        return avtaleRepository.findById(id)
                .map(avtale -> ResponseEntity.ok(avtale))
                .orElseThrow(ResourceNotFoundException::new);
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
                                      @RequestHeader("If-Match") Integer versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        return avtaleRepository.findById(avtaleId)
                .map(avtale -> {
                    avtale.sjekkVersjon(versjon);
                    avtale.endreAvtale(endreAvtale);
                    Avtale lagretAvtale = avtaleRepository.save(avtale);
                    return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon().toString()).build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
