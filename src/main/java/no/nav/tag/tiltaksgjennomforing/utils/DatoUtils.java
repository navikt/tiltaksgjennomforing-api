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

    public static Instant localDateTimeTilInstant(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
            .map(DatoUtils::localDateTimeTilZonedDateTime)
            .map(ZonedDateTime::toInstant)
            .orElse(null);
    }

    public static ZonedDateTime localDateTimeTilZonedDateTime(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault());
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

    public static ZonedDateTime instantTilZonedDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault());
    }

    public static boolean harDatoPassert(LocalDate dato) {
        return Optional.ofNullable(dato)
            .map(d -> d.isBefore(Now.localDate()))
            .orElse(false);
    }

}
