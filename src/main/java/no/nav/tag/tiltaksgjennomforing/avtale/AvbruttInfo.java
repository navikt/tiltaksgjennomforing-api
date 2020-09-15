package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvbruttInfo {
    private LocalDate avbruttDato;
    private String avbruttGrunn;

    public void grunnErOppgitt() {
        if (avbruttGrunn == null || avbruttGrunn.isEmpty()) {
            throw new TiltaksgjennomforingException("Grunn til avbrytelse av avtale må oppgis");
        }
    }
}
