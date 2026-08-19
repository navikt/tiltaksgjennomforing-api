package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.InnsatsgruppeException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MidlertidigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static final int TILSKUDDSPROSENT = 40;
    public static final int TILSKUDDSPROSENT_TILPASSET = 60;
    public static final int TILSKUDDSPROSENT_REDUKSJONSFAKTOR = 10;

    public MidlertidigLonnstilskuddAvtaleBeregningStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public Integer getProsentForForstePeriode() {
        return switch (avtale.getInnsatsgruppe()) {
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> TILSKUDDSPROSENT_TILPASSET;
            case TRENGER_VEILEDNING -> TILSKUDDSPROSENT;
            case null, default -> throw InnsatsgruppeException.fraTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        };
    }

    @Override
    public Integer getProsentForPeriode(AvtaleInnhold avtaleInnhold, Periode periode) {
        Optional<LocalDate> datoForRedusertProsent = getDatoerForReduksjon(avtaleInnhold).stream().findFirst();

        Integer tilskuddsprosent = getProsentForForstePeriode();
        if (tilskuddsprosent == null) {
            return null;
        }

        boolean erRedusert = datoForRedusertProsent
            .map(dato -> !periode.getSlutt().isBefore(dato))
            .orElse(false);

        return erRedusert ? tilskuddsprosent - TILSKUDDSPROSENT_REDUKSJONSFAKTOR : tilskuddsprosent;
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(AvtaleInnhold avtaleInnhold) {
        LocalDate startDato = avtaleInnhold.getStartDato();
        LocalDate sluttDato = avtaleInnhold.getSluttDato();

        if (startDato == null || sluttDato == null) {
            return Collections.emptyList();
        }

        LocalDate datoForReduksjon = switch (avtale.getInnsatsgruppe()) {
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> startDato.plusYears(1);
            case TRENGER_VEILEDNING -> startDato.plusMonths(6);
            case null, default -> null;
        };

        if (datoForReduksjon == null || datoForReduksjon.isAfter(sluttDato)) {
            return Collections.emptyList();
        }

        return List.of(datoForReduksjon);
    }
}
