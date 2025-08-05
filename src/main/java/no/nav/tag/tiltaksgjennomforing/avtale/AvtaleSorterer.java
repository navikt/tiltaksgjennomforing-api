package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

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
            case VEILEDER -> getSortingOrderVeileder(sortOrder, sortDirection);
            case BESLUTTER -> getSortingOrderBeslutter(sortOrder, sortDirection);
            default -> getSortingOrderDeltakerOgArbeidsgiver(sortOrder, sortDirection);
        };
    }

    private static Sort getSortingOrderVeileder(SortOrder order, Sort.Direction direction) {
        Sort sortById = Sort.by(direction, "id");
        return switch (order) {
            case BEDRIFTNAVN -> Sort.by(direction, "gjeldendeInnhold.bedriftNavn").and(sortById);
            case DELTAKERFORNAVN -> Sort.by(direction, "gjeldendeInnhold.deltakerFornavn").and(sortById);
            case DELTAKERETTERNAVN -> Sort.by(direction, "gjeldendeInnhold.deltakerEtternavn").and(sortById);
            case OPPRETTETTIDSPUNKT -> Sort.by(direction, "opprettetTidspunkt").and(sortById);
            case SLUTTDATO -> Sort.by(direction, "gjeldendeInnhold.sluttDato").and(sortById);
            case STARTDATO -> Sort.by(direction, "gjeldendeInnhold.startDato").and(sortById);
            case TILTAKSTYPE -> Sort.by(direction, "tiltakstype").and(sortById);
            case VEILEDERNAVIDENT -> Sort.by(direction, "veilederNavIdent").and(sortById);
            case STATUS -> Sort.unsorted()
                .and(JpaSort.unsafe(direction, "CASE WHEN (oppfolgingVarselSendt IS NOT NULL) THEN 0 ELSE 1 END"))
                .and(JpaSort.unsafe(direction, "CASE WHEN (t.status = 'AVSLÅTT') THEN 0 ELSE 1 END"))
                .and(Sort.by(direction, "status"))
                .and(sortById);
            default -> Sort.unsorted()
                .and(JpaSort.unsafe(direction, "CASE WHEN (oppfolgingVarselSendt IS NOT NULL) THEN 1 ELSE 0 END"))
                .and(JpaSort.unsafe(direction, "CASE WHEN (t.status = 'AVSLÅTT') THEN 1 ELSE 0 END"))
                .and(Sort.by(direction, "sistEndret"))
                .and(sortById);
        };
    }

    private static Sort getSortingOrderBeslutter(
        SortOrder order,
        Sort.Direction direction
    ) {
        Sort sortById = Sort.by(direction, "id");
        return switch (order) {
            case BEDRIFTNAVN -> Sort.by(direction, "bedriftNavn").and(sortById);
            case DELTAKERFORNAVN -> Sort.by(direction, "deltakerFornavn").and(sortById);
            case DELTAKERETTERNAVN -> Sort.by(direction, "deltakerEtternavn").and(sortById);
            case OPPRETTETTIDSPUNKT -> Sort.by(direction, "opprettetTidspunkt").and(sortById);
            case STARTDATO -> Sort.by(direction, "startDato").and(sortById);
            case TILTAKSTYPE -> Sort.by(direction, "tiltakstype").and(sortById);
            case STATUS -> Sort.by(direction, "harReturnertSomKanBehandles")
                .and(Sort.by(direction, "status"))
                .and(Sort.by(direction, "antallUbehandlet"))
                .and(sortById);
            default -> Sort.by(direction, "harReturnertSomKanBehandles")
                .and(Sort.by(direction, "sistEndret"))
                .and(sortById);
        };
    }

    private static Sort getSortingOrderDeltakerOgArbeidsgiver(
        SortOrder order,
        Sort.Direction direction
    ) {
        Sort sortById = Sort.by(direction, "id");
        return switch (order) {
            case BEDRIFTNAVN -> Sort.by(direction, "gjeldendeInnhold.bedriftNavn").and(sortById);
            case DELTAKERFORNAVN -> Sort.by(direction, "gjeldendeInnhold.deltakerFornavn").and(sortById);
            case DELTAKERETTERNAVN -> Sort.by(direction, "gjeldendeInnhold.deltakerEtternavn").and(sortById);
            case OPPRETTETTIDSPUNKT -> Sort.by(direction, "opprettetTidspunkt").and(sortById);
            case SLUTTDATO -> Sort.by(direction, "gjeldendeInnhold.sluttDato").and(sortById);
            case STARTDATO -> Sort.by(direction, "gjeldendeInnhold.startDato").and(sortById);
            case STATUS -> Sort.by(direction, "status").and(sortById);
            case TILTAKSTYPE -> Sort.by(direction, "tiltakstype").and(sortById);
            default -> Sort.by(direction, "sistEndret").and(sortById);
        };
    }
}
