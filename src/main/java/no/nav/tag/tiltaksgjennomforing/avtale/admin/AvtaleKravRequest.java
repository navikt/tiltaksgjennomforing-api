package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import java.time.LocalDateTime;

public record AvtaleKravRequest(
    LocalDateTime avtaleKravTidspunkt
) {
}
