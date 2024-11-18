package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAnnullert;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeForkortet;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeGodkjent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Profile({ Miljø.LOCAL, Miljø.DEV_GCP_LABS, Miljø.DEV_FSS_Q0 })
public class TilskuddsperiodeFakeKafkaProducer {
    private final RestTemplate restTemplate;
    private final String url;

    public TilskuddsperiodeFakeKafkaProducer(RestTemplate noAuthRestTemplate, @Value("${tiltaksgjennomforing.kafka.fake-url}") String url) {
        this.restTemplate = noAuthRestTemplate;
        this.url = url;
    }

    @TransactionalEventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        TilskuddsperiodeGodkjentMelding melding = TilskuddsperiodeGodkjentMelding.create(event.getAvtale(), event.getTilskuddsperiode(), event.getResendingsnummer());
        try {
            restTemplate.exchange(url + "/tilskuddsperiode-godkjent", HttpMethod.POST, new HttpEntity<>(melding), Void.class);
        } catch (RestClientException e) {
            log.warn("Feil ved kall til tiltak-refusjon-api", e);
        }
    }

    @TransactionalEventListener
    public void tilskuddsperiodeAnnullert(TilskuddsperiodeAnnullert event) {
        TilskuddsperiodeAnnullertMelding melding = new TilskuddsperiodeAnnullertMelding(event.getTilskuddsperiode().getId(), TilskuddsperiodeAnnullertÅrsak.AVTALE_ANNULLERT);
        try {
            restTemplate.exchange(url + "/tilskuddsperiode-annullert", HttpMethod.POST, new HttpEntity<>(melding), Void.class);
        } catch (RestClientException e) {
            log.warn("Feil ved kall til tiltak-refusjon-api", e);
        }
    }

    @TransactionalEventListener
    public void tilskuddsperiodeForkortet(TilskuddsperiodeForkortet event) {
        TilskuddsperiodeForkortetMelding melding = new TilskuddsperiodeForkortetMelding(event.getTilskuddsperiode().getId(), event.getTilskuddsperiode().getBeløp(), event.getTilskuddsperiode().getSluttDato());
        try {
            restTemplate.exchange(url + "/tilskuddsperiode-forkortet", HttpMethod.POST, new HttpEntity<>(melding), Void.class);
        } catch (RestClientException e) {
            log.warn("Feil ved kall til tiltak-refusjon-api", e);
        }
    }
}
