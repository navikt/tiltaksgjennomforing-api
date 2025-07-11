package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@UtilityClass
public class DatoUtils {
    public static LocalDate sisteDatoIMnd(LocalDate dato) {
        return LocalDate.of(dato.getYear(), dato.getMonth(), dato.lengthOfMonth());
    }

    public static LocalDate maksDato(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    public static LocalDateTime instantTilLocalDateTime(Instant instant) {
        return Optional.ofNullable(instant)
            .map(DatoUtils::instantTilZonedDateTime)
            .map(ZonedDateTime::toLocalDateTime)
            .orElse(null);
    }

    public static LocalDate instantTilLocalDate(Instant instant) {
        return Optional.ofNullable(instant)
            .map(DatoUtils::instantTilZonedDateTime)
            .map(ZonedDateTime::toLocalDate)
            .orElse(null);
    }

    private static ZonedDateTime instantTilZonedDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("Europe/Oslo"));
    }
}
