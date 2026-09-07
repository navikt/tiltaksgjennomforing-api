package no.nav.tag.tiltaksgjennomforing.avtale.service;

import java.util.UUID;

public record OppdaterAvtalestatusRespons(
    UUID sisteId,
    boolean harFlere,
    int antallOppdatert
) {}
