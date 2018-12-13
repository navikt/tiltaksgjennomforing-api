package no.nav.tag.tiltaksgjennomforing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.Utils.lagUri;

@RestController
@RequestMapping("/api")
public class AvtaleController {

    private AvtaleRepository avtaleRepository;
    private OppgaveRepository oppgaveRepository;
    private MaalRepository maalRepository;

    @Autowired
    public AvtaleController(AvtaleRepository avtaleRepository,
                            OppgaveRepository oppgaveRepository,
                            MaalRepository maalRepository) {
        this.avtaleRepository = avtaleRepository;
        this.oppgaveRepository = oppgaveRepository;
        this.maalRepository = maalRepository;
    }

    @GetMapping("/avtaler/{id}")
    public Avtale hent(@PathVariable("id") Integer id) {
        Avtale avtale = avtaleRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        List<Oppgave> oppgaver = oppgaveRepository.hentOppgaverForAvtale(id);
        List<Maal> maal = maalRepository.hentMaalForAvtale(id);
        avtale.setOppgaver(oppgaver);
        avtale.setMaal(maal);
        return avtale;
    }

    @GetMapping("/avtaler")
    public Iterable<Avtale> hentAlle() {
        return avtaleRepository.findAll();
    }

    @PostMapping("/avtaler")
    public ResponseEntity opprettAvtale(@RequestBody String deltakerFodselsnr) {
        if (deltakerFodselsnr == null) {
            return ResponseEntity.badRequest().build();
        } else {
            Avtale avtale = Avtale.nyAvtale(deltakerFodselsnr);
            Avtale opprettetAvtale = avtaleRepository.save(avtale);
            URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
            return ResponseEntity.created(uri).build();
        }
    }

    @PostMapping("/avtaler/{avtaleId}/maal")
    public ResponseEntity opprettMaal(@PathVariable("avtaleId") Integer avtaleId, @RequestBody Maal maal) {
        if (avtaleRepository.existsById(avtaleId)) {
            maal.setAvtale(avtaleId);
            Maal lagretMaal = maalRepository.save(maal);
            URI uri = lagUri("/avtaler/" + avtaleId + "/maal/" + lagretMaal.getId());
            return ResponseEntity.created(uri).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/avtaler/{avtaleId}/oppgaver")
    public ResponseEntity opprettOppgave(@PathVariable("avtaleId") Integer avtaleId, @RequestBody Oppgave oppgave) {
        if (avtaleRepository.existsById(avtaleId)) {
            oppgave.setAvtale(avtaleId);
            Oppgave lagretOppgave = oppgaveRepository.save(oppgave);
            URI uri = lagUri("/avtaler/" + avtaleId + "/oppgaver/" + lagretOppgave.getId());
            return ResponseEntity.created(uri).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: endreOppgave, endreMaal

    @PutMapping("/avtaler/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") Integer avtaleId, @RequestBody Avtale avtale) {
        if (avtaleRepository.existsById(avtaleId)) {
            Avtale gammelAvtale = avtaleRepository.findById(avtaleId).get();
            // Ikke endre id eller opprettetTidspunkt
            avtale.setId(gammelAvtale.getId());
            avtale.setOpprettetTidspunkt(gammelAvtale.getOpprettetTidspunkt());
            avtaleRepository.save(avtale);
            return ResponseEntity.ok().build();
        } {
            return ResponseEntity.notFound().build();
        }
    }
}
