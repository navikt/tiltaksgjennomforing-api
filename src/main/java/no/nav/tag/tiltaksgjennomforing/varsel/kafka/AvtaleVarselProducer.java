package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiverErFordelt;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class AvtaleVarselProducer {

    private final KafkaTemplate<String, String> aivenKafkaTemplate;

    private final ObjectMapper objectMapper;

    public AvtaleVarselProducer(
            @Qualifier("aivenKafkaTemplate") KafkaTemplate<String, String> aivenKafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.aivenKafkaTemplate = aivenKafkaTemplate;
        this.objectMapper = objectMapper;
    }


    @TransactionalEventListener
    public void avtaleEndret(AvtaleEndret event) {
        this.publiserMelding(AvtalePubliseringsType.ENDRET, event.getAvtale());
    }

    @TransactionalEventListener
    public void avtaleOpprettetAvVeileder(AvtaleOpprettetAvVeileder event) {
        this.publiserMelding(AvtalePubliseringsType.OPPRETTET_VEILEDER, event.getAvtale());
    }

    @TransactionalEventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        this.publiserMelding(AvtalePubliseringsType.OPPRETTET_ARBEIDSGIVER, event.getAvtale());
    }

    @TransactionalEventListener
    public void overtaAvtale(AvtaleOpprettetAvArbeidsgiverErFordelt event) {
        this.publiserMelding(AvtalePubliseringsType.AVTALE_FORDELT, event.getAvtale());
    }

    protected void publiserMelding(AvtalePubliseringsType key, Avtale avtale) {
        AvtalePublisering avtalePublisering = new AvtalePublisering(key, avtale);
        UUID meldingId = UUID.randomUUID();
        String melding = null;
        try {
            melding = objectMapper.writeValueAsString(avtalePublisering);
        } catch (JsonProcessingException exception) {
            log.error("greide ikke parse object om til JSON melding med id {} til topic {}", meldingId, Topics.AVTALE_VARSEL);
            log.error("feilmelding", exception);
            return;
        }
        aivenKafkaTemplate.send(Topics.AVTALE_VARSEL, key.name(), melding)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Melding med id {} sendt til kafka topic {} feilet", meldingId, Topics.AVTALE_VARSEL);
                    }

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.info("Melding med id {} sendt til kafka topic {} var vellykket", meldingId, Topics.AVTALE_VARSEL);
                    }
                });

    }
}
