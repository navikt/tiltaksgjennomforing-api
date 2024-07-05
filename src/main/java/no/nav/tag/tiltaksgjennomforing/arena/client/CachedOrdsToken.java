package no.nav.tag.tiltaksgjennomforing.arena.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CachedOrdsToken {
    private final OrdsToken token;
    private final LocalDateTime cachedAt = LocalDateTime.now();

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
