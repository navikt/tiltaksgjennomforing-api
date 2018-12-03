package no.nav.tag.tiltaksgjennomforing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AvtaleController {

    private AvtaleRepository repository;

    @Autowired
    public AvtaleController(AvtaleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/avtaler/{id}")
    public Avtale hent(@PathVariable("id") Integer id) {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @GetMapping("/avtaler")
    public Iterable<Avtale> hentAlle() {
        return repository.findAll();
    }

    @PostMapping("/avtaler")
    public Integer opprett(@RequestBody Avtale avtale) {
        Avtale opprettetAvtale = repository.save(avtale);
        return opprettetAvtale.getId();
    }

    @PutMapping("/avtaler/{id}")
    public void settAvtale(@PathVariable("id") Integer id, @RequestBody Avtale avtale) {
        if (repository.existsById(id)) {
            avtale.setId(id);
        }
        repository.save(avtale);
    }
}
