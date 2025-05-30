package no.nav.tag.tiltaksgjennomforing.leader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class LeaderPodCheck {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String path;
    private final boolean enabled;

    public LeaderPodCheck(RestTemplate noAuthRestTemplate, ObjectMapper objectMapper, @Value("${ELECTOR_PATH}") String electorPath) {
        this.restTemplate = noAuthRestTemplate;
        this.objectMapper = objectMapper;
        this.enabled = isNotBlank(electorPath);
        this.path = "http://" + electorPath;
    }

    @Retryable(backoff = @Backoff(delayExpression = "${tiltaksgjennomforing.retry.delay}", maxDelayExpression = "${tiltaksgjennomforing.retry.max-delay}", multiplier = 2))
    public boolean isLeaderPod() {
        if (!enabled) {
            return true;
        }
        String hostname;
        String leader;
        try {
            leader = getJSONFromUrl(path).name;
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Feil v/henting av host for leader-election", e);
            return false;
        } catch (Exception e) {
            log.error("Feil v/oppslag i leader-elector", e);
            throw new RuntimeException(e);
        }
        return hostname.equals(leader);
    }

    private Elector getJSONFromUrl(String electorPath) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        var entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(electorPath, HttpMethod.GET, entity, String.class);
        return objectMapper.readValue(responseEntity.getBody(), Elector.class);
    }

    @Data
    private static class Elector {
        String name;
    }
}
