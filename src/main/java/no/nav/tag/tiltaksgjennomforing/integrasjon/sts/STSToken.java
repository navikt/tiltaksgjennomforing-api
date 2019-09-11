package no.nav.tag.tiltaksgjennomforing.integrasjon.sts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class STSToken {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    int expiresIn;
}
