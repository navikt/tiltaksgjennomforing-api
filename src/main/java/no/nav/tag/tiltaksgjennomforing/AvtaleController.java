package no.nav.tag.tiltaksgjennomforing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AvtaleController {

    @Autowired
    private AvtaleRepository repository;

    @RequestMapping("/avtaler/{id}")
    public Avtale hent(@PathVariable("id") Integer id) {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @RequestMapping("/avtaler")
    public Iterable<Avtale> hentAlle() {
        return repository.findAll();
    }

    @PostMapping("/avtaler")
    public Integer opprett(@RequestBody Avtale avtale) {
        Avtale opprettetAvtale = repository.save(avtale);
        return opprettetAvtale.getId();
    }
}
