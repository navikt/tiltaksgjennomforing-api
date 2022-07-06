package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
    public void avtaleEndret(AvtaleEndret avtaleEndret) {
        String avtaleId = avtaleEndret.getAvtale().getId().toString();
        this.publiserMelding(Topics.AVTALE_ENDRET, avtaleId, AvtalePubliseringsType.AVTALE_ENDRET, avtaleEndret);
    }

    @TransactionalEventListener
    public void avtaleOpprettetAvVeileder(AvtaleOpprettetAvVeileder avtaleOpprettetAvVeileder) {
        String avtaleId = avtaleOpprettetAvVeileder.getAvtale().getId().toString();
        this.publiserMelding(Topics.AVTALE_OPPRETTET, avtaleId, AvtalePubliseringsType.AVTALE_OPPRETTET_AV_VEILEDER, avtaleOpprettetAvVeileder);
    }

    @TransactionalEventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver avtaleOpprettetAvArbeidsgiver) {
        String avtaleId = avtaleOpprettetAvArbeidsgiver.getAvtale().getId().toString();
        this.publiserMelding(Topics.AVTALE_OPPRETTET, avtaleId, AvtalePubliseringsType.AVTALE_OPPRETTET_AV_ARBEIDSGIVER, avtaleOpprettetAvArbeidsgiver);
    }

    @TransactionalEventListener
    public void overtaAvtale(AvtaleOpprettetAvArbeidsgiverErFordelt avtaleOpprettetAvArbeidsgiverErFordelt) {
        String avtaleId = avtaleOpprettetAvArbeidsgiverErFordelt.getAvtale().getId().toString();
        this.publiserMelding(Topics.AVTALE_ENDRET, avtaleId, AvtalePubliseringsType.AVTALE_OPPRETTET_AV_ARBEIDSGIVER_ER_FORDELT, avtaleOpprettetAvArbeidsgiverErFordelt);
    }

    protected void publiserMelding(String topic, String meldingId, String key,  Object innkommendeMelding) {
        String melding = null;
        try {
            melding = objectMapper.writeValueAsString(innkommendeMelding);
        } catch (JsonProcessingException exception) {
            log.error("greide ikke parse object om til JSON melding med id {} til topic {}", meldingId, topic);
            log.error("feilmelding", exception);
            return;
        }
        aivenKafkaTemplate.send(topic, key, melding)
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Melding med id {} sendt til kafka topic {} feilet", meldingId, topic);
                    }

                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        log.info("Melding med id {} sendt til kafka topic {} var vellykket", meldingId, topic);
                    }
                });

    }
}
