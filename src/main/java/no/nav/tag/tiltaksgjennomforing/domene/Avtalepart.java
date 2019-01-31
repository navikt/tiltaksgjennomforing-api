package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

@AllArgsConstructor
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;
    final Avtale avtale;

    public abstract void endreGodkjenning(boolean godkjenning);

    public abstract boolean kanEndreAvtale();

    public abstract Rolle rolle();

    public void endreAvtale(Integer versjon, EndreAvtale endreAvtale) {
        if (!kanEndreAvtale()) {
            throw new TilgangskontrollException("Kan ikke endre avtale.");
        }
        avtale.endreAvtale(versjon, endreAvtale);
    }

    public enum Rolle {
        DELTAKER, ARBEIDSGIVER, VEILEDER
    }
}
