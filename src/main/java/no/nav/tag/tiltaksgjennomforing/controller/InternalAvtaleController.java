package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.domene.journalfoering.AvtaleTilJournalfoering;
import no.nav.tag.tiltaksgjennomforing.domene.journalfoering.AvtaleTilJournalfoeringMapper;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Protected
@RestController
@RequestMapping("/internal/avtaler")
@Timed
@RequiredArgsConstructor
public class InternalAvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    public Iterable<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        innloggingService.validerSystembruker();
            return StreamSupport.stream(avtaleRepository.findAll().spliterator(), true)
                .filter(avtale -> avtale.getJournalpostId() == null)
                .filter(avtale -> avtale.erGodkjentAvVeileder())
                .map(avtale -> AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale))
                .collect(Collectors.toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity journalfoerAvtaler(@RequestBody Map<UUID, String> avtalerTilJournalfoert){
        innloggingService.validerSystembruker();
        Iterable<Avtale> avtaler = avtaleRepository.findAllById(avtalerTilJournalfoert.keySet());
        avtaler.forEach(avtale -> avtale.setJournalpostId(avtalerTilJournalfoert.get(avtale.getId())));
        avtaleRepository.saveAll(avtaler);
        return ResponseEntity.ok().build();
    }
}


