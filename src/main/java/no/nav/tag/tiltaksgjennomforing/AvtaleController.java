package no.nav.tag.tiltaksgjennomforing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
