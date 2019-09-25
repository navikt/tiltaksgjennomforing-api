package no.nav.tag.tiltaksgjennomforing.varsel.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;

@Value
public class VarslbarHendelseOppstaatt {
    private final Avtale avtale;
    private final VarslbarHendelse varslbarHendelse;
    private final GamleVerdier gamleVerdier;
}
