package no.nav.tag.tiltaksgjennomforing.datavarehus;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.data.domain.Slice;

public record DvhAvtalePatcherRespons(
    Slice<Avtale> slice,
    Integer antallAvtalerSendt
) {}
