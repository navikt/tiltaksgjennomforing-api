package no.nav.tag.tiltaksgjennomforing.dokgen;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoeringMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DokgenService {
    private final DokgenProperties dokgenProperties;

    public byte[] avtalePdf(Avtale avtale) {
        var avtaleTilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale.gjeldendeInnhold());
        return restOperations().postForObject(dokgenProperties.getUri(), avtaleTilJournalfoering, byte[].class);
    }

    // Lager ny instans av RestOperations i stedet for å wire inn RestTemplate fordi det var vanskelig å få den til å bruke en ObjectMapper som hadde datoer på format 'yyyy-MM-dd' i stedet for et array
    private RestOperations restOperations() {
        RestTemplate rest = new RestTemplate();
        //this is crucial!
        rest.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
        return rest;
    }

    private MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        var converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    private ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
