package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;

@Value
public class AvtaleInngått {
    Avtale avtale;
    AvtaleHendelseUtførtAvRolle utførtAvRolle;
    NavIdent utførtAv;
}
