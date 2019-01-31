package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

@AllArgsConstructor
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;

    public abstract void endreGodkjenning(Avtale avtale, boolean godkjenning);

    public abstract boolean kanEndreAvtale();

    public abstract Rolle rolle();

    public void endreAvtale(Avtale avtale, Integer versjon, EndreAvtale endreAvtale) {
        if (!kanEndreAvtale()) {
            throw new TilgangskontrollException("Kan ikke endre avtale.");
        }
        avtale.endreAvtale(versjon, endreAvtale);
    }

    public enum Rolle {
        DELTAKER, ARBEIDSGIVER, VEILEDER, INGEN_ROLLE
    }
}
