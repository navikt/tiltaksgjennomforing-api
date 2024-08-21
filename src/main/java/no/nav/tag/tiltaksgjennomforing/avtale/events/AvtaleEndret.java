package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;

@Value
public class AvtaleEndret {
    Avtale avtale;
    AvtaleHendelseUtførtAvRolle utfortAvRolle;
    Identifikator utfortAv;
}
