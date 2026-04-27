package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import no.nav.tag.tiltaksgjennomforing.varsel.oppgave.GosysVarselType;

import java.util.UUID;

public record AvtaleGosysVarselRequest(
    UUID avtale,
    GosysVarselType type
) {
}
