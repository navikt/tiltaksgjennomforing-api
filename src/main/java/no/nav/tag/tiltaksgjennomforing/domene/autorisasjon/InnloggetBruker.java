package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

import java.util.List;

@Data
public abstract class InnloggetBruker<T extends Identifikator> {
    private final T identifikator;

    public abstract Avtalepart avtalepart(Avtale avtale);

    public boolean harTilgang(Avtale avtale) {
        return avtalepart(avtale) != null;
    }

    public void sjekkTilgang(Avtale avtale) {
        if (!harTilgang(avtale)) {
            throw new TilgangskontrollException("Har ikke tilgang til avtalen.");
        }
    }

    public List<Identifikator> identifikatorer() {
        return List.of(identifikator);
    }
}
