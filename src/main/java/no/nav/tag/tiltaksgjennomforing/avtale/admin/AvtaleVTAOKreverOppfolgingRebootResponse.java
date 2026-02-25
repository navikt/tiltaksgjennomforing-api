package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import no.nav.tag.tiltaksgjennomforing.avtale.Status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AvtaleVTAOKreverOppfolgingRebootResponse(
    UUID avtaleId,
    Integer avtaleNr,
    Status status,
    Instant sistKjenteOppfolging,
    LocalDate nyOppfolging
) {
}
