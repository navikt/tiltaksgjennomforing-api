package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

import java.util.Set;
import java.util.UUID;

@Value
public class InkluderingstilskuddsutgifterFjernet {
    Avtale avtale;
    Set<UUID> utgiftIder;
}
