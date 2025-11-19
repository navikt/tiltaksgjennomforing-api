package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import lombok.RequiredArgsConstructor;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.arena.client.acl.AktivitetArenaAclClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.hendelse.HendelseAktivitetsplanClient;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
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

@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@RestController
@RequestMapping("/utvikler-admin/aktivitetsplan")
@RequiredArgsConstructor
public class AktivitetsplanAdminController {
    private final AvtaleRepository avtaleRepository;
    private final AktivitetArenaAclClient aktivitetArenaAclClient;
    private final HendelseAktivitetsplanClient hendelseAktivitetsplanClient;
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;

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

    private UUID hentAktivitetsplanIdForAvtale(UUID avtaleId) {
        List<Integer> deltakerIder = arenaAgreementMigrationRepository.findTiltakdeltakerIdFromAvtaleId(avtaleId);

        if (deltakerIder.size() > 1) {
            throw new IllegalArgumentException("Fant mer enn 1 deltaker for avtale " + avtaleId);
        }

        Integer deltakerId = deltakerIder.stream().findFirst().orElseThrow(RessursFinnesIkkeException::new);
        return aktivitetArenaAclClient.getAktivitetsId(deltakerId);
    }

    @PostMapping("/ta-over-flere-kort")
    public void taOverAktivitetsplankortForFlereAvtaler(@RequestBody List<UUID> avtaleIder) {
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
