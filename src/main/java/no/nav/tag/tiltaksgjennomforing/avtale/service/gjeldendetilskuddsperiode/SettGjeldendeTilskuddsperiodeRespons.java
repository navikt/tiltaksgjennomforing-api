package no.nav.tag.tiltaksgjennomforing.avtale.service.gjeldendetilskuddsperiode;

import java.util.UUID;

public record SettGjeldendeTilskuddsperiodeRespons(
    UUID sisteId,
    boolean harFlere,
    int antallOppdatert,
    int antallIkkeOppdatert
) {}
