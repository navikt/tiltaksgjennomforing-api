package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MidlertidigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static final int TILSKUDDSPROSENT = 40;
    public static final int TILSKUDDSPROSENT_TILPASSET = 60;
    public static final int TILSKUDDSPROSENT_REDUKSJONSFAKTOR = 10;

    public Integer getProsentForForstePeriode(Avtale avtale) {
        return switch (avtale.getKvalifiseringsgruppe()) {
            case SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS -> TILSKUDDSPROSENT_TILPASSET;
            case SITUASJONSBESTEMT_INNSATS -> TILSKUDDSPROSENT;
            case null, default -> null;
        };
    }

    @Override
    public Integer getProsentForPeriode(Avtale avtale, Periode periode) {
        Optional<LocalDate> datoForRedusertProsent = getDatoerForReduksjon(avtale).stream().findFirst();

        Integer tilskuddsprosent = getProsentForForstePeriode(avtale);
        if (tilskuddsprosent == null) {
            return null;
        }

        boolean erRedusert = datoForRedusertProsent
            .map(dato -> !periode.getSlutt().isBefore(dato))
            .orElse(false);

        return erRedusert ? tilskuddsprosent - TILSKUDDSPROSENT_REDUKSJONSFAKTOR : tilskuddsprosent;
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale) {
        LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate sluttDato = avtale.getGjeldendeInnhold().getSluttDato();

        if (startDato == null || sluttDato == null) {
            return Collections.emptyList();
        }

        LocalDate datoForReduksjon = switch (avtale.getKvalifiseringsgruppe()) {
            case SPESIELT_TILPASSET_INNSATS, VARIG_TILPASSET_INNSATS -> startDato.plusYears(1);
            case SITUASJONSBESTEMT_INNSATS -> startDato.plusMonths(6);
            case null, default -> null;
        };

        if (datoForReduksjon == null || datoForReduksjon.isAfter(sluttDato)) {
            return Collections.emptyList();
        }

        return List.of(datoForReduksjon);
    }
}
