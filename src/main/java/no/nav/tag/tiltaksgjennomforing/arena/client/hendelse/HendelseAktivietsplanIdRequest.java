package no.nav.tag.tiltaksgjennomforing.arena.client.hendelse;

import java.util.UUID;

public record HendelseAktivietsplanIdRequest(
    UUID aktivitetsplanId,
    boolean resendSisteMelding
) {}
