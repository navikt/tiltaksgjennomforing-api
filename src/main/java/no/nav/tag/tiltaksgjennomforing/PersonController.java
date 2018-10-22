package no.nav.tag.tiltaksgjennomforing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {
    @Autowired
    private PersonRepository repository;

    @RequestMapping("/person/{id}")
    public Person hent(@PathVariable("id") Integer id) {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @RequestMapping("/person")
    public Iterable<Person> hentAlle() {
        return repository.findAll();
    }
}
