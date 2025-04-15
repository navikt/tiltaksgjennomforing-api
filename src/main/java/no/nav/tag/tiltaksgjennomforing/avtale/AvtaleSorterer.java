package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Sorter sorter = new Sorter(new Sort.Order(direction, "id"));
        return switch (order) {
            case BEDRIFTNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.bedriftNavn"));
            case DELTAKERFORNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.deltakerFornavn"));
            case DELTAKERETTERNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> sorter.add(new Sort.Order(direction, "opprettetTidspunkt"));
            case SLUTTDATO -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.sluttDato"));
            case STARTDATO -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.startDato"));
            case TILTAKSTYPE -> sorter.add(new Sort.Order(direction, "tiltakstype"));
            case VEILEDERNAVIDENT -> sorter.add(new Sort.Order(direction, "veilederNavIdent"));
            case STATUS -> sorter.add(
                // NULLS_LAST fungerer ikke i postgres, derfor må vi sortere i revers for å få oppfølging øverst
                new Sort.Order(direction, "oppfolgingVarselSendt").reverse(),
                new Sort.Order(direction, "status")
            );
            default -> sorter.add(
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
        Sorter sorter = new Sorter(new Sort.Order(direction, "id"));
        return switch (order) {
            case BEDRIFTNAVN -> sorter.add(new Sort.Order(direction, "bedriftNavn"));
            case DELTAKERFORNAVN -> sorter.add(new Sort.Order(direction, "deltakerFornavn"));
            case DELTAKERETTERNAVN -> sorter.add(new Sort.Order(direction, "deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> sorter.add(new Sort.Order(direction, "opprettetTidspunkt"));
            case STARTDATO -> sorter.add(new Sort.Order(direction, "startDato"));
            case STATUS -> sorter.add(new Sort.Order(direction, "antallUbehandlet"));
            case TILTAKSTYPE -> sorter.add(new Sort.Order(direction, "tiltakstype"));
            default -> sorter.add(new Sort.Order(direction, "sistEndret"));
        };
    }

    private static List<Sort.Order> getSortingOrderDeltakerOgArbeidsgiver(
        SortOrder order,
        Sort.Direction direction
    ) {
        Sorter sorter = new Sorter(new Sort.Order(direction, "id"));
        return switch (order) {
            case BEDRIFTNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.bedriftNavn"));
            case DELTAKERFORNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.deltakerFornavn"));
            case DELTAKERETTERNAVN -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.deltakerEtternavn"));
            case OPPRETTETTIDSPUNKT -> sorter.add(new Sort.Order(direction, "opprettetTidspunkt"));
            case SLUTTDATO -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.sluttDato"));
            case STARTDATO -> sorter.add(new Sort.Order(direction, "gjeldendeInnhold.startDato"));
            case STATUS -> sorter.add(new Sort.Order(direction, "status"));
            case TILTAKSTYPE -> sorter.add(new Sort.Order(direction, "tiltakstype"));
            default -> sorter.add(new Sort.Order(direction, "sistEndret"));
        };
    }

    static class Sorter {
        private Sort.Order seed;

        public Sorter(Sort.Order seed) {
            this.seed = seed;
        }

        public List<Sort.Order> add(Sort.Order... orders) {
            List<Sort.Order> order = new ArrayList<>(Arrays.asList(orders));
            order.add(seed);
            return order;
        }
    }
}
