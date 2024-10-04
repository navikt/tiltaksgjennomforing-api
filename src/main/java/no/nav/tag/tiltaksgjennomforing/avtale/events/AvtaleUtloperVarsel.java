package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

@Value
public class AvtaleUtloperVarsel {
    public enum Type {
        OM_EN_UKE,
        OM_24_TIMER
    }

    Avtale avtale;
    Type type;
}
