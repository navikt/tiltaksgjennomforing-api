package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class Utils {
    public static <T> T sjekkAtIkkeNull(T in, String feilmelding) {
        if (in == null) {
            throw new TiltaksgjennomforingException(feilmelding);
        }
        return in;
    }

    public static boolean erIkkeTomme(Object... objekter) {
        for (Object objekt : objekter) {
            if (objekt instanceof String && ((String) objekt).isEmpty()) {
                return false;
            }
            if (objekt instanceof Collection && ((Collection<?>) objekt).isEmpty()) {
                return false;
            }
            if (objekt == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean erNoenTomme(Object... objekter) {
        return !erIkkeTomme(objekter);
    }

    public static boolean erTom(Object objekt) {
        return !erIkkeTomme(objekt);
    }

    public static URI lagUri(String path) {
        return UriComponentsBuilder
                .fromPath(path)
                .build()
                .toUri();
    }

    public static void sjekkAtTekstIkkeOverskrider1000Tegn(String tekst, String feilmelding) {
        if ((tekst != null) && tekst.length() > 1000) {
            throw new TiltaksgjennomforingException(feilmelding);
        }
    }

    public static void fikseLøpenumre(List<TilskuddPeriode> tilskuddperioder, int startPåLøpenummer) {
        for (int i = 0; i < tilskuddperioder.size(); i++) {
            tilskuddperioder.get(i).setLøpenummer(startPåLøpenummer + i);
        }
    }

    public static boolean equalsMenIkkeNull(Object a, Object b) {
        return a != null && b != null && Objects.equals(a, b);
    }

    public static BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    public static Integer convertBigDecimalToInt(BigDecimal value) {
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
