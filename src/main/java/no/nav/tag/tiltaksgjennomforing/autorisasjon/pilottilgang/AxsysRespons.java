package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AxsysRespons {
    private List<AxsysEnhet> enheter;

    List<NavEnhet> tilEnheter() {
        return enheter.stream().map(enhet -> new NavEnhet(enhet.getEnhetId())).collect(Collectors.toList());
    }
}
