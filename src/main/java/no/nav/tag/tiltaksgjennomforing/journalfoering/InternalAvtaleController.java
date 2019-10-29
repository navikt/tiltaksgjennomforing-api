package no.nav.tag.tiltaksgjennomforing.journalfoering;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/avtaler")
@Timed
@RequiredArgsConstructor
@ProtectedWithClaims(issuer="system")
public class InternalAvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    public List<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        innloggingService.validerSystembruker();
        List<UUID> avtaleIdList = avtaleRepository.finnAvtaleIdTilJournalfoering();
        return avtaleRepository.findAllById(avtaleIdList).stream()
                .map(AvtaleTilJournalfoeringMapper::tilJournalfoering)
                .collect(Collectors.toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<?> journalfoerAvtaler(@RequestBody Map<UUID, String> avtalerTilJournalfoert) {
        innloggingService.validerSystembruker();
        Iterable<Avtale> avtaler = avtaleRepository.findAllById(avtalerTilJournalfoert.keySet());
        avtaler.forEach(avtale -> avtale.setJournalpostId(avtalerTilJournalfoert.get(avtale.getId())));
        avtaleRepository.saveAll(avtaler);
        return ResponseEntity.ok().build();
    }
}


