package no.nav.tag.tiltaksgjennomforing.avtale.service.gjeldendetilskuddsperiode;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.data.domain.Slice;

public record SettGjeldendeTilskuddsperiodeRespons(
    Slice<Avtale> slice,
    Integer antallOppdatert,
    Integer antallIkkeOppdatert
) {}
