package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class RefusjonVarselConsumer {
    private final AvtaleRepository avtaleRepository;
    private final ObjectMapper objectMapper;

    public RefusjonVarselConsumer(AvtaleRepository avtaleRepository, ObjectMapper objectMapper) {
        this.avtaleRepository = avtaleRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.TILTAK_VARSEL)
    public void consume(String melding) throws JsonProcessingException {
        RefusjonVarselMelding refusjonVarselMelding = objectMapper.readValue(melding, RefusjonVarselMelding.class);
        Avtale avtale = avtaleRepository.findById(refusjonVarselMelding.getAvtaleId()).orElseThrow(RuntimeException::new);
        VarselType varselType = refusjonVarselMelding.getVarselType();

        try {
            switch (varselType) {
                case KLAR -> avtale.refusjonKlar(refusjonVarselMelding.getFristForGodkjenning());
                case REVARSEL -> avtale.refusjonRevarsel(refusjonVarselMelding.getFristForGodkjenning());
                case FRIST_FORLENGET -> avtale.refusjonFristForlenget();
                case KORRIGERT -> avtale.refusjonKorrigert();
            }
            avtaleRepository.save(avtale);
        } catch (FeilkodeException e) {
            if (e.getFeilkode() == Feilkode.KAN_IKKE_ENDRE_ANNULLERT_AVTALE) {
                log.warn("Avtale med id {} har ugyldig status, varsler derfor ikke om: {}", refusjonVarselMelding.getAvtaleId(), varselType);
            } else {
                throw e;
            }
        }
    }
}
