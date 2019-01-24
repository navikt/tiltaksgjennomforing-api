package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public abstract class Person<T extends PersonIdentifikator> {
    protected T identifikator;

    public void endreGodkjenning(Avtale avtale, EndreGodkjenning endreGodkjenning) {
        avtale.endreGodkjenning(this, endreGodkjenning.getGodkjent());
    }
}
