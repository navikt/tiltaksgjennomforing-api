package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Oppfølgingsbruker {

    @JsonProperty("fodselsnr")
    public String fnr;

    @JsonProperty("nav_kontor")
    public String navKontor;
}
