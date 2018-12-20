package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
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
    public Avtale hent(@PathVariable("id") Integer id) {
        return avtaleRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @GetMapping
    public Iterable<Avtale> hentAlle() {
        return avtaleRepository.findAll();
    }

    @PostMapping
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtaleRequest opprettAvtaleRequest) {
        Avtale opprettetAvtale = avtaleRepository.save(opprettAvtaleRequest.create());
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") Integer avtaleId, @RequestBody Avtale nyAvtale) {
        return avtaleRepository.findById(avtaleId)
                .map(avtale -> {
                    avtale.endreAvtale(nyAvtale);
                    avtaleRepository.save(avtale);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
