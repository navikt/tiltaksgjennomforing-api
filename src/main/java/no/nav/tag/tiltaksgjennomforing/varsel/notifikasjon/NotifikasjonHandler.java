package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.KallTiArbeidsgiverNotifikasjonFeiletException;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.CommonResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class NotifikasjonHandler {
    private final ObjectMapper objectMapper;
    private final ArbeidsgiverNotifikasjonRepository notifikasjonRepository;

    NotifikasjonHandler(
            @Autowired ObjectMapper objectMapper,
            ArbeidsgiverNotifikasjonRepository repository
    ) {
        this.objectMapper = objectMapper;
        this.notifikasjonRepository = repository;
    }

    public <T> T readResponse(String json, Class<T> contentClass) {
        try {
            return objectMapper.readValue(json, contentClass);
        } catch (IOException exception) {
            log.error("objectmapper feilet med lesing av data: {}", exception.getMessage());
        }
        return null;
    }

    public CommonResponse convertResponse(Object data) {
        if (data != null) {
            return objectMapper.convertValue(data, CommonResponse.class);
        }
        return null;
    }

    public void sjekkOgSettStatusResponse(
            ArbeidsgiverNotifikasjon notifikasjon,
            CommonResponse response,
            MutationStatus vellykketStatus) {
            if (response == null){
                throw new KallTiArbeidsgiverNotifikasjonFeiletException();
            }
            if (response.get__typename().equals(vellykketStatus.getStatus())) {
                notifikasjon.setHendelseUtfort(true);
            }
            notifikasjon.setStatus(vellykketStatus.getStatus());
            notifikasjonRepository.save(notifikasjon);
    }
}
