package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.UtviklerTilgangProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/utvikler-admin/tilskuddsperioder")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "aad")
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Slf4j
public class InternalTilskuddsperiodeController {
    private final TilskuddsperiodeKafkaProducer tilskuddsperiodeKafkaProducer;
    private final AvtaleRepository avtaleRepository;
    private final UtviklerTilgangProperties utviklerTilgangProperties;
    private final TokenUtils tokenUtils;

    private void sjekkTilgang() {
        if (!tokenUtils.harAdGruppe(utviklerTilgangProperties.getGruppeTilgang())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/send-godkjenn-melding")
    @Transactional
    public void sendTilskuddsperiodeGodkjent(@RequestBody SendTilskuddsperiodeGodkjentRequest request) {
        sjekkTilgang();
        // Endepunkt for å sende melding til Kafka om en tilskuddsperiode som ikke ble sendt pga. toggle som feilaktig var avskrudd
        Avtale avtale = avtaleRepository.findById(request.getAvtaleId()).orElseThrow();
        TilskuddPeriode tilskuddPeriode = avtale.getTilskuddPeriode().stream().filter(tp -> tp.getId().equals(request.getTilskuddsperiodeId())).findFirst().orElseThrow();
        TilskuddsperiodeGodkjentMelding melding = TilskuddsperiodeGodkjentMelding.create(avtale, tilskuddPeriode);
        tilskuddsperiodeKafkaProducer.publiserTilskuddsperiodeGodkjentMelding(melding);
    }

    @Value
    private static class SendTilskuddsperiodeGodkjentRequest {
        UUID tilskuddsperiodeId;
        UUID avtaleId;
    }
}
