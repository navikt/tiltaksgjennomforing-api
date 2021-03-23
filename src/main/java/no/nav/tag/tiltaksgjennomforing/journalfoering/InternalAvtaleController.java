package no.nav.tag.tiltaksgjennomforing.journalfoering;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdRepository;
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

    private static final String journalfører = "PÅGÅR";
    private final AvtaleInnholdRepository avtaleInnholdRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    @Transactional
    public List<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        innloggingService.validerSystembruker();
        List<AvtaleInnhold> avtaleVersjoner = avtaleInnholdRepository.finnAvtaleVersjonerTilJournalfoering();
        avtaleVersjoner.forEach(avtaleInnhold -> avtaleInnhold.setJournalpostId(journalfører));
        return avtaleInnholdRepository.saveAll(avtaleVersjoner)
                .stream()
                .map(AvtaleTilJournalfoeringMapper::tilJournalfoering)
                .collect(Collectors.toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<?> journalfoerAvtaler(@RequestBody Map<UUID, String> avtaleVersjonerTilJournalfoert) {
        innloggingService.validerSystembruker();
        Iterable<AvtaleInnhold> avtaleVersjoner = avtaleInnholdRepository.findAllById(avtaleVersjonerTilJournalfoert.keySet());
        avtaleVersjoner.forEach(avtaleVersjon -> avtaleVersjon.setJournalpostId(avtaleVersjonerTilJournalfoert.get(avtaleVersjon.getId())));
        avtaleInnholdRepository.saveAll(avtaleVersjoner);
        return ResponseEntity.ok().build();
    }
}