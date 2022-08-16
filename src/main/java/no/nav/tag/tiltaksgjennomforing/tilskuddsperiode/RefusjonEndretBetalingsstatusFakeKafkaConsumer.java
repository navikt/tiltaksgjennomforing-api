package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.fake")
@RestController
@Unprotected
@RequestMapping("/refusjon/utbetalt")
@Slf4j
@RequiredArgsConstructor
public class RefusjonEndretBetalingsstatusFakeKafkaConsumer {

    private final TilskuddPeriodeRepository tilskuddPeriodeRepository;

    @PostMapping
    public void refusjonUtbetalt(RefusjonEndretBetalingsstatusMelding melding) {
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(UUID.fromString(melding.getTilskuddsperiodeId())).orElseThrow();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UTBETALT);
        tilskuddPeriodeRepository.save(tilskuddPeriode);
    }
}
