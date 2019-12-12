package no.nav.tag.tiltaksgjennomforing.journalfoering;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
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
@ProtectedWithClaims(issuer = "system")
public class InternalAvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final AvtaleInnholdRepository avtaleInnholdRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    public List<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        innloggingService.validerSystembruker();
        List<UUID> uuidList = avtaleInnholdRepository.finnAvtaleIdTilJournalfoering();
        return avtaleRepository.findAllById(uuidList).stream()
                .filter(avtale -> avtale.getTiltakstype() == Tiltakstype.ARBEIDSTRENING)
                .flatMap(avtale -> avtale.getVersjoner().stream()
                        .filter(AvtaleInnhold::skalJournalfores)
                        .map(avtaleInnhold -> AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale, avtaleInnhold)))
                .collect(Collectors.toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<?> journalfoerAvtaler(@RequestBody Map<UUID, String> avtaleVersjonerTilJournalfoert) {
        innloggingService.validerSystembruker();
        Iterable<AvtaleInnhold> avtaleVersjoner = avtaleInnholdRepository.findAllById(avtaleVersjonerTilJournalfoert.keySet());
        avtaleVersjoner.forEach(avtale -> avtale.setJournalpostId(avtaleVersjonerTilJournalfoert.get(avtale.getId())));
        avtaleInnholdRepository.saveAll(avtaleVersjoner);
        return ResponseEntity.ok().build();
    }
}


