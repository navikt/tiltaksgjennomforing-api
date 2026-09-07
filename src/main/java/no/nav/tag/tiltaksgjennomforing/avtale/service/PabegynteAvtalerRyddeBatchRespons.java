package no.nav.tag.tiltaksgjennomforing.avtale.service;

import java.util.UUID;

public record PabegynteAvtalerRyddeBatchRespons(
    UUID sisteId,
    boolean harFlere,
    int antallUtlop,
    int antallVarsel24Timer,
    int antallVarselEnUke
) {}
