package no.nav.tag.tiltaksgjennomforing.journalfoering;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/internal/avtaler")
@Timed
@RequiredArgsConstructor
@Unprotected
public class InternalAvtaleController {

    private final static List<AvtaleTilJournalfoering> TOM_LISTE = Collections.emptyList();

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    public Iterable<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        innloggingService.validerSystembruker();
        Iterable<UUID> avtaleIdList = avtaleRepository.finnAvtaleIdTilJournalfoering();

        if(!avtaleIdList.iterator().hasNext()){
            return TOM_LISTE;
        }

        return StreamSupport.stream(avtaleRepository.findAllById(avtaleIdList).spliterator(), false)
                .map(avtale -> AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale))
                .collect(Collectors.toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity journalfoerAvtaler(@RequestBody Map<UUID, String> avtalerTilJournalfoert) {
        innloggingService.validerSystembruker();
        Iterable<Avtale> avtaler = avtaleRepository.findAllById(avtalerTilJournalfoert.keySet());
        avtaler.forEach(avtale -> avtale.setJournalpostId(avtalerTilJournalfoert.get(avtale.getId())));
        avtaleRepository.saveAll(avtaler);
        return ResponseEntity.ok().build();
    }
}


