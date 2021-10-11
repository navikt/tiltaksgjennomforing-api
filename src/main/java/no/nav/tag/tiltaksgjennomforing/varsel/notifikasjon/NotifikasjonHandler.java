package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.KallTiArbeidsgiverNotifikasjonFeiletException;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.CommonResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.FellesMutationResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    public FellesMutationResponse convertResponse(Object data) {
        try {
            if (data != null) {
                return objectMapper.convertValue(data, FellesMutationResponse.class);
            }
        }catch (Exception e) {
            log.error("feilet med convertering av data til FellesMutationResponse klasse");
        }
        return null;
    }

    public void sjekkOgSettStatusResponse(
            ArbeidsgiverNotifikasjon notifikasjon,
            FellesMutationResponse response,
            MutationStatus vellykketStatus) {
        if (response != null) {
            if (response.get__typename().equals(vellykketStatus.getStatus())) {
                notifikasjon.setHendelseUtfort(true);
            }
            notifikasjon.setStatus(vellykketStatus.getStatus());
            notifikasjonRepository.save(notifikasjon);
        }
    }

    public void saveNotifikasjon(ArbeidsgiverNotifikasjon notifikasjon) {
        notifikasjonRepository.save(notifikasjon);
    }

    public List<ArbeidsgiverNotifikasjon> finnAktiveNotifikasjonerUtfoert(UUID id) {
        return notifikasjonRepository.findArbeidsgiverNotifikasjonByAvtaleId(id);
    }
}
