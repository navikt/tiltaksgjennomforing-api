package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlleredePaaTiltakRequest(
        @JsonProperty(required = true)
        Fnr deltakerFnr,
        @JsonProperty(required = true)
        Tiltakstype tiltakstype,
        String startDato,
        String sluttDato,
        String avtaleId
) {
}
