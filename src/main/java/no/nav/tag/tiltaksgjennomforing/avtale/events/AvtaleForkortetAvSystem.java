package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

import java.time.LocalDate;

@Value
public class AvtaleForkortetAvSystem {
    Avtale avtale;
    AvtaleInnhold avtaleInnhold;
    LocalDate nySluttDato;
    String grunn;
    Identifikator utførtAv;
}
