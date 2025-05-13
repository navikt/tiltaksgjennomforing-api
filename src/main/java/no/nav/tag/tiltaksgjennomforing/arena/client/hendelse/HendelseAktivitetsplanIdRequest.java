package no.nav.tag.tiltaksgjennomforing.arena.client.hendelse;

import java.util.UUID;

public record HendelseAktivitetsplanIdRequest(
    UUID aktivitetsplanId,
    boolean resendSisteMelding
) {}
