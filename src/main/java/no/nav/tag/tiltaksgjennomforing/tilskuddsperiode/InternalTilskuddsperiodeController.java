package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Unprotected
@RequiredArgsConstructor
@RequestMapping("/internal/tilskuddsperiode")
public class InternalTilskuddsperiodeController {
    private final TilskuddsperiodeKafkaProducer tilskuddsperiodeKafkaProducer;
    private final AvtaleRepository avtaleRepository;


    @PostMapping("/send-godkjenn-melding")
    @Transactional
    public void sendTilskuddsperiodeGodkjent(@RequestBody SendTilskuddsperiodeGodkjentRequest request) {
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
