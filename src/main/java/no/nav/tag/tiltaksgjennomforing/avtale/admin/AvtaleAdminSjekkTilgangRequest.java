package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import java.util.UUID;

public record AvtaleAdminSjekkTilgangRequest(
    // Kan hentes via Azure CLI: `az ad user show --id {Nav e-postadresse}`
    UUID veilederAzureOid,
    boolean brukAktorId
) {}
