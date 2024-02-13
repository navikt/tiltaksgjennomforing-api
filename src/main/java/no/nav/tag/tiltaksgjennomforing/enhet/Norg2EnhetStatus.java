package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Norg2EnhetStatus {
    UNDER_ETABLERING("Under etablering"),
    AKTIV("Aktiv"),
    UNDER_AVVIKLING("Under avvikling"),
    NEDLAGT("Nedlagt");

    @JsonValue
    private final String status;
}
