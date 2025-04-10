package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;

import java.time.Instant;
import java.util.UUID;

public record AuditElement(
        UUID id,
        Instant sistEndret,
        Fnr deltakerFnr,
        BedriftNr bedriftNr
) {
    public static AuditElement of(AuditerbarEntitet entitet) {
        return new AuditElement(
                entitet.getId(),
                entitet.getSistEndret(),
                entitet.getFnrOgBedrift().deltakerFnr(),
                entitet.getFnrOgBedrift().bedriftNr()
        );
    }

    public static AuditElement fraException(IkkeTilgangTilDeltakerException ikkeTilgangTilDeltakerException) {
        return new AuditElement(
                null, null, ikkeTilgangTilDeltakerException.getFnr(), null
        );
    }
}
