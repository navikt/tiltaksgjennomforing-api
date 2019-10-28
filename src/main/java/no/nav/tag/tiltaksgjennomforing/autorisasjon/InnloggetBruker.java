package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

import java.util.List;

@Data
public abstract class InnloggetBruker<T extends Identifikator> {
    private final T identifikator;

    public abstract Avtalepart<T> avtalepart(Avtale avtale);

    public abstract boolean harLeseTilgang(Avtale avtale);

    public void sjekkLeseTilgang(Avtale avtale) {
        if (!harLeseTilgang(avtale)) {
            throw new TilgangskontrollException("Har ikke tilgang til avtalen.");
        }
    }

    public abstract boolean harSkriveTilgang(Avtale avtale);

    public void sjekkSkriveTilgang(Avtale avtale) {
        if (!harSkriveTilgang(avtale)) {
            throw new TilgangskontrollException("Har ikke tilgang til avtalen.");
        }
    }

    public List<Identifikator> identifikatorer() {
        return List.of(identifikator);
    }

    @JsonProperty("erNavAnsatt")
    public abstract boolean erNavAnsatt();
}
