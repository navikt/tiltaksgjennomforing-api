package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn;

import java.time.LocalDate;

@Value
public class AvtaleForkortetAvArena {
    Avtale avtale;
    AvtaleInnhold avtaleInnhold;
    LocalDate nySluttDato;
    ForkortetGrunn forkortetGrunn;
}
