package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/utvikler-admin/tilskuddsperioder")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Slf4j
public class TilskuddsperiodeAdminController {
    private final TilskuddsperiodeKafkaProducer tilskuddsperiodeKafkaProducer;
    private final TilskuddPeriodeRepository tilskuddPeriodeRepository;

    // Generer en kafkamelding og send den. Oppdaterer ikke statuser eller lignende på perioden
    @PostMapping("/send-tilskuddsperiode-godkjent-melding/{tilskuddsperiodeId}")
    public void sendTilskuddsperiodeGodkjentMelding(@PathVariable("tilskuddsperiodeId") UUID id, @RequestParam(value = "resendingsnummer", required = false) Integer resendingsnummer) {
        log.info("Lager og sender tilskuddsperiode godkjent-melding for tilskuddsperiode: {}", id);
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
        Avtale avtale = tilskuddPeriode.getAvtale();
        TilskuddsperiodeGodkjentMelding melding = TilskuddsperiodeGodkjentMelding.create(avtale, tilskuddPeriode, resendingsnummer);
        tilskuddsperiodeKafkaProducer.publiserTilskuddsperiodeGodkjentMelding(melding);

    }

    // Generer en kafkamelding og send den. Oppdaterer ikke statuser eller lignende på perioden
    @PostMapping("/send-tilskuddsperiode-annullert-melding/{tilskuddsperiodeId}")
    public void sendTilskuddsperiodeAnnullertMelding(@PathVariable("tilskuddsperiodeId") UUID id) {
        log.info("Lager og sender tilskuddsperiode annullert-melding for tilskuddsperiode: {}", id);
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
        TilskuddsperiodeAnnullertMelding melding = new TilskuddsperiodeAnnullertMelding(tilskuddPeriode.getId(), TilskuddsperiodeAnnullertÅrsak.AVTALE_ANNULLERT);
        tilskuddsperiodeKafkaProducer.publiserTilskuddsperiodeAnnullertMelding(melding);
    }

}
