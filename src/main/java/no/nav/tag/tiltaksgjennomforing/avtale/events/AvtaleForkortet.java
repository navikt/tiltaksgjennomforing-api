package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;

import java.time.LocalDate;

@Value
public class AvtaleForkortet {
    Avtale avtale;
    AvtaleInnhold avtaleInnhold;
    LocalDate nySluttDato;
    String grunn;
    String annetGrunn;
}
