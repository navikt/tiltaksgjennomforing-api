package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDateTime;

@Getter
public class CachedOrdsToken {
    private final OrdsToken token;
    private final LocalDateTime cachedAt = Now.localDateTime();

    public CachedOrdsToken(OrdsToken token) {
        this.token = token;
    }

    public record OrdsToken(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("expires_in")
        long expiresIn
    ) {}
}
