package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;

@Value
public class AvtaleEndret {
    Avtale avtale;
    AvtaleHendelseUtførtAv.Rolle utfortAvRolle;
    Identifikator utfortAv;
}
