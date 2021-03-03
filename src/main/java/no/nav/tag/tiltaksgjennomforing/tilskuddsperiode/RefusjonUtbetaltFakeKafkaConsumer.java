package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.fake")
@RestController
@Unprotected
@RequestMapping("/refusjon/utbetalt")
@Slf4j
@RequiredArgsConstructor
public class RefusjonUtbetaltFakeKafkaConsumer {
    private final AvtaleRepository avtaleRepository;

    @PostMapping
    public void refusjonUtbetalt(RefusjonUtbetaltMelding melding) {
        Avtale avtale = avtaleRepository.findById(melding.getAvtaleId()).orElseThrow();
        TilskuddPeriode tilskuddPeriode = avtale.getTilskuddPeriode().stream().filter(it -> it.getId().equals(melding.getTilskuddsperiodeId())).findFirst().orElseThrow();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtaleRepository.save(avtale);
    }
}
