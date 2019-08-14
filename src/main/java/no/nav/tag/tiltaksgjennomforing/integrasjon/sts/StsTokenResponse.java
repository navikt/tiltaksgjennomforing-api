package no.nav.tag.tiltaksgjennomforing.integrasjon.sts;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class StsTokenResponse {
    @JsonAlias(value = "access_token")
    private String accessToken;

    @JsonAlias(value = "token_type")
    private String tokenType;

    @JsonAlias(value = "expires_in")
    private String expiresIn;

}
