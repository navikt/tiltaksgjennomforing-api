package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.oidc.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity opprettAvtale(@RequestBody Fnr deltakerFnr) {
        if (deltakerFnr == null) {
            return ResponseEntity.badRequest().build();
        } else {
            Avtale opprettetAvtale = avtaleRepository.save(Avtale.nyAvtale(deltakerFnr));
            URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
            return ResponseEntity.created(uri).build();
        }
    }

    @PostMapping("/{avtaleId}/maal")
    public ResponseEntity endreMaal(@PathVariable("avtaleId") Integer avtaleId, @RequestBody List<Maal> maal) {
        return avtaleRepository.findById(avtaleId)
                .map(avtale -> {
                    avtale.setMaal(maal);
                    avtaleRepository.save(avtale);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{avtaleId}/oppgaver")
    public ResponseEntity endreOppgaver(@PathVariable("avtaleId") Integer avtaleId, @RequestBody List<Oppgave> oppgaver) {
        return avtaleRepository.findById(avtaleId)
                .map(avtale -> {
                    avtale.setOppgaver(oppgaver);
                    avtaleRepository.save(avtale);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") Integer avtaleId, @RequestBody Avtale avtale) {
        Optional<Avtale> avtaleOptional = avtaleRepository.findById(avtaleId);
        if (avtaleOptional.isPresent()) {
            Avtale gammelAvtale = avtaleOptional.get();
            // Ikke endre id eller opprettetTidspunkt
            avtale.setId(gammelAvtale.getId());
            avtale.setOpprettetTidspunkt(gammelAvtale.getOpprettetTidspunkt());
            avtaleRepository.save(avtale);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
