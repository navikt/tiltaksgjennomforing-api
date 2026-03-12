package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import java.time.LocalDate;

public record Tilskuddstrinn(
    LocalDate start,
    LocalDate slutt,
    Integer prosent,
    Integer belopPerMnd
) {}
