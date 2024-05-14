package no.nav.tag.tiltaksgjennomforing.journalfoering;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnholdRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/avtaler")
@Timed
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "system")
@Slf4j
public class InternalAvtaleController {

    private final AvtaleInnholdRepository avtaleInnholdRepository;
    private final InnloggingService innloggingService;

    @GetMapping
    public List<AvtaleTilJournalfoering> hentIkkeJournalfoerteAvtaler() {
        try {
            innloggingService.validerSystembruker();
            List<AvtaleInnhold> avtaleVersjoner = avtaleInnholdRepository.finnAvtaleVersjonerTilJournalfoering();
            List<AvtaleTilJournalfoering> avtalerTilJournalfoering = avtaleVersjoner.stream().map(avtaleInnhold -> {
                List<TilskuddPeriode> tilskuddPeriode = avtaleInnhold.getAvtale().getTilskuddPeriode();
                AvtaleTilJournalfoering avtaleTilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtaleInnhold, null);
                avtaleTilJournalfoering.setTilskuddsPerioder(tilskuddPeriode);
                return avtaleTilJournalfoering;
            }).collect(Collectors.toList());
            return avtalerTilJournalfoering;
        } catch (Exception e) {
            log.error("Feil ved henting av ikke-journalførte avtaler", e);
            throw e;
        }
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
