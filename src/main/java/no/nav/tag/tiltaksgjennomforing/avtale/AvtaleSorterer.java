package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.List;

@UtilityClass
public class AvtaleSorterer {
    enum SortOrder {
        BEDRIFTNAVN,
        DELTAKERFORNAVN,
        DELTAKERETTERNAVN,
        OPPRETTETTIDSPUNKT,
        SISTENDRET,
        SLUTTDATO,
        STARTDATO,
        STATUS,
        TILTAKSTYPE,
        VEILEDERNAVIDENT,
    }

    static Sort getSortingOrder(Avtalerolle rolle, String order, String direction) {
        SortOrder sortOrder = SortOrder.valueOf(order.toUpperCase());
        Sort.Direction sortDirection = Sort.Direction.valueOf(direction.toUpperCase());

        return switch (rolle) {
            case VEILEDER -> Sort.by(getSortingOrderVeileder(sortOrder, sortDirection));
            case BESLUTTER -> Sort.by(getSortingOrderBeslutter(sortOrder, sortDirection));
            default -> Sort.by(getSortingOrderDeltakerOgArbeidsgiver(sortOrder, sortDirection));
        };
    }

    private static List<Sort.Order> getSortingOrderVeileder(SortOrder order, Sort.Direction direction) {
        return switch (order) {
            case BEDRIFTNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.bedriftNavn"));
            case DELTAKERFORNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.deltakerFornavn"));
            case DELTAKERETTERNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> List.of(new Sort.Order(direction, "opprettetTidspunkt"));
            case SLUTTDATO -> List.of(new Sort.Order(direction, "gjeldendeInnhold.sluttDato"));
            case STARTDATO -> List.of(new Sort.Order(direction, "gjeldendeInnhold.startDato"));
            case TILTAKSTYPE -> List.of(new Sort.Order(direction, "tiltakstype"));
            case VEILEDERNAVIDENT -> List.of(new Sort.Order(direction, "veilederNavIdent"));
            case STATUS -> List.of(
                // NULLS_LAST fungerer ikke i postgres, derfor må vi sortere i revers for å få oppfølging øverst
                new Sort.Order(direction, "oppfolgingVarselSendt").reverse(),
                new Sort.Order(direction, "status")
            );
            default -> List.of(
                // NULLS_LAST fungerer ikke i postgres, derfor må vi sortere i revers for å få oppfølging øverst
                new Sort.Order(direction, "oppfolgingVarselSendt").reverse(),
                new Sort.Order(direction, "sistEndret")
            );
        };
    }

    private static List<Sort.Order> getSortingOrderBeslutter(
        SortOrder order,
        Sort.Direction direction
    ) {
        return switch (order) {
            case BEDRIFTNAVN -> List.of(new Sort.Order(direction, "bedriftNavn"));
            case DELTAKERFORNAVN -> List.of(new Sort.Order(direction, "deltakerFornavn"));
            case DELTAKERETTERNAVN -> List.of(new Sort.Order(direction, "deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> List.of(new Sort.Order(direction, "opprettetTidspunkt"));
            case STARTDATO -> List.of(new Sort.Order(direction, "startDato"));
            case STATUS -> List.of(new Sort.Order(direction, "antallUbehandlet"));
            case TILTAKSTYPE -> List.of(new Sort.Order(direction, "tiltakstype"));
            default -> List.of(new Sort.Order(direction, "sistEndret"));
        };
    }

    private static List<Sort.Order> getSortingOrderDeltakerOgArbeidsgiver(
        SortOrder order,
        Sort.Direction direction
    ) {
        return switch (order) {
            case BEDRIFTNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.bedriftNavn"));
            case DELTAKERFORNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.deltakerFornavn"));
            case DELTAKERETTERNAVN -> List.of(new Sort.Order(direction, "gjeldendeInnhold.deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> List.of(new Sort.Order(direction, "opprettetTidspunkt"));
            case SLUTTDATO -> List.of(new Sort.Order(direction, "gjeldendeInnhold.sluttDato"));
            case STARTDATO -> List.of(new Sort.Order(direction, "gjeldendeInnhold.startDato"));
            case STATUS -> List.of(new Sort.Order(direction, "status"));
            case TILTAKSTYPE -> List.of(new Sort.Order(direction, "tiltakstype"));
            default -> List.of(new Sort.Order(direction, "sistEndret"));
        };
    }
}
