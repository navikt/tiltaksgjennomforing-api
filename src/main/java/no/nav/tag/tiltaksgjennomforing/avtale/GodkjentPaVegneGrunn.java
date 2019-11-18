package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class GodkjentPaVegneGrunn {
    private boolean ikkeBankId;
    private boolean reservert;
    private boolean digitalKompetanse;

    public void valgtMinstEnGrunn() {
        if (!ikkeBankId && !reservert && !digitalKompetanse) {
            throw new TiltaksgjennomforingException("Minst én grunn må være valgt");
        }
    }
}

