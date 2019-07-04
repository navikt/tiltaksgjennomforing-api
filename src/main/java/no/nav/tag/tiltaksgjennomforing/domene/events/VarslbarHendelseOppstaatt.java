package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;

@Value
public class VarslbarHendelseOppstaatt {
    private final Avtale avtale;
    private final VarslbarHendelse varslbarHendelse;
}
