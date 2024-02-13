package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HentEnhetResponse {
    private String enhetNr;
    private String navn;
}
