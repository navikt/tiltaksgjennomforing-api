package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class DatoUtils {
    public static LocalDate sisteDatoIMnd(LocalDate dato) {
        return LocalDate.of(dato.getYear(), dato.getMonth(), dato.lengthOfMonth());
    }

    public static LocalDate maksDato(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }
}