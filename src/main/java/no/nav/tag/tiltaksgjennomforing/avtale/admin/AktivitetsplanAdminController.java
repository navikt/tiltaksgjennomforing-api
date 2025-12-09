package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.arena.client.acl.AktivitetArenaAclClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.hendelse.HendelseAktivitetsplanClient;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakdeltaker;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaOrdsFnrRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakdeltakerRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@RestController
@RequestMapping("/utvikler-admin/aktivitetsplan")
@RequiredArgsConstructor
public class AktivitetsplanAdminController {
    private final AvtaleRepository avtaleRepository;
    private final AktivitetArenaAclClient aktivitetArenaAclClient;
    private final HendelseAktivitetsplanClient hendelseAktivitetsplanClient;
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;
    private final ArenaTiltakdeltakerRepository arenaTiltakdeltakerRepository;
    private final ArenaOrdsFnrRepository arenaOrdsFnrRepository;

    /*
     * Tar over aktivitetsplankortet fra Arena og sender siste melding på nytt.
     * Brukes dersom et tiltak ikke ble migrert fra Arena, men Aktivitetsplankortet ennå henger igjen med feil status.
     */
    @PostMapping("/avtale/{avtaleId}/ta-over-kort")
    public void taOverAktivitetsplankort(@PathVariable UUID avtaleId) {
        UUID aktivitetsplanId = hentAktivitetsplanIdForAvtale(avtaleId);
        hendelseAktivitetsplanClient.putAktivitetsplanId(avtaleId, aktivitetsplanId, true);
    }

    /*
     * Hent aktivitetsplan-id som vi vil brukt dersom vi tok over kort fra Arena.
     */
    @PostMapping("/avtale/{avtaleId}/hent-aktivitetsplan-id")
    public ResponseEntity<UUID> hentAktivitetsplanId(@PathVariable UUID avtaleId) {
        return ok(hentAktivitetsplanIdForAvtale(avtaleId));
    }
    /** I enkelte tilfeller har veiledere opprettet 2, (eller flere) tiltak hos oss, det har dukket opp 2 forbered tiltaksgjennomføringer i Arena,
     * med fnr som en kommentar, men veileder har puttet fnr fra avtale 1  i avtale 2 i arena og omvendt.
     * Arena takler ikke nye meldinger på missmatch fnr-saker.**/
    private boolean erMissMatchFnr (UUID avtaleId) {
        // Finn fnr fra Arena-gjennomføring
        Integer deltakerId = finnDeltakerIdForAvtale(avtaleId);
        ArenaTiltakdeltaker arenaTiltakdeltaker = arenaTiltakdeltakerRepository.findByTiltakdeltakerId(deltakerId).getFirst();
        String fnrIArena = arenaOrdsFnrRepository.findByPersonId(arenaTiltakdeltaker.getPersonId()).getFirst().getFnr();
        // Finn fnr fra avtale
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        // Sammenlign
        return !avtale.getDeltakerFnr().asString().equals(fnrIArena);
    }

    private Integer finnDeltakerIdForAvtale(UUID avtaleId) {
        List<Integer> deltakerIder = arenaAgreementMigrationRepository.findTiltakdeltakerIdFromAvtaleId(avtaleId);

        if (deltakerIder.size() > 1) {
            throw new IllegalArgumentException("Fant mer enn 1 deltaker for avtale " + avtaleId);
        }

        Integer deltakerId = deltakerIder.stream().findFirst().orElseThrow(RessursFinnesIkkeException::new);
        return deltakerId;
    }

    private UUID hentAktivitetsplanIdForAvtale(UUID avtaleId) {
        Integer deltakerId = finnDeltakerIdForAvtale(avtaleId);
        return aktivitetArenaAclClient.getAktivitetsId(deltakerId);
    }

    @PostMapping("/ta-over-flere-kort")
    public void taOverAktivitetsplankortForFlereAvtaler(@RequestBody List<UUID> avtaleIder, boolean dryRun) {
        List<UUID> utenMissmatch = avtaleIder.stream().filter(avtaleId -> !erMissMatchFnr(avtaleId)).toList();
        List<UUID> medMissMatch = avtaleIder.stream().filter(this::erMissMatchFnr).toList();
        log.info("Kjører ta-over-flere-kort for {} avtaler uten fnr-missmatch (av totalt {})", utenMissmatch.size(), avtaleIder.size());
        log.info("Avtaler med fnr-missmatch: {}", medMissMatch);
        if (dryRun) {
            log.info("Dry run - gjør ingen endringer");
            return;
        }
        avtaleIder.forEach(avtale -> {
            taOverAktivitetsplankort(avtale);
            });
    }

    /*
     * Sender siste melding på nytt til aktivitetsplanen.
     * Brukes dersom vi har fått feil fra aktivitetsplanen med kollisjon på melding-id.
     */
    @PostMapping("/avtale/{avtaleId}/send-siste-melding")
    public void sendSisteMeldingPaaNytt(@PathVariable UUID avtaleId) {
        avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        hendelseAktivitetsplanClient.postSendSisteMelding(avtaleId);
    }

    /**
     * Genererer en ny aktivitetsplan id og sender siste melding på nytt.
     * Brukes dersom deltaker har falt ut av oppfølging.
     */
    @PostMapping("/avtale/{avtaleId}/generer-ny-id")
    public void genererNyId(@PathVariable UUID avtaleId) {
        avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        hendelseAktivitetsplanClient.putAktivitetsplanId(avtaleId, UUID.randomUUID(), true);
    }

}
