package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.Comparator;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class AvtaleSorterer {
    public Comparator<Avtale> comparatorForAvtale(String sorteringskolonne) {
        return switch (sorteringskolonne) {
            case Avtale.Fields.opprettetTidspunkt ->
                    Comparator.comparing(Avtale::getOpprettetTidspunkt, Comparator.reverseOrder());
            case AvtaleInnhold.Fields.bedriftNavn ->
                    Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getBedriftNavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerEtternavn ->
                    Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getDeltakerEtternavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerFornavn ->
                    Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getDeltakerFornavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(Avtale::status);
            case "startDato" ->
                    Comparator.comparing(avtale -> (avtale.gjeldendeTilskuddsperiode() != null ? avtale.gjeldendeTilskuddsperiode().getStartDato() : avtale.getGjeldendeInnhold().getStartDato()), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Avtale::getSistEndret, Comparator.reverseOrder());
        };
    }

    private static String lowercaseEllerNull(String x) {
        return x != null ? x.toLowerCase() : null;
    }

    static Sort.Order getSortingOrderForPageableVeileder(String sorteringskolonne) {
        return getSortingOrderForPageableVeileder(sorteringskolonne, "ASC");
    }

    static Sort.Order getSortingOrderForPageableVeileder(String sorteringskolonne, String sorteringsRetning) {
        var feldtnavn = switch (sorteringskolonne) {
            case "deltakerFornavn" -> "gjeldendeInnhold.deltakerFornavn";
            case "opprettetTidspunkt" -> "opprettetTidspunkt";
            case "bedriftNavn" -> "gjeldendeInnhold.bedriftNavn";
            case "startDato" -> "gjeldendeInnhold.startDato";
            case "tiltakstype" -> "tiltakstype";
            default -> "sistEndret";
        };
        return new Sort.Order(Sort.Direction.fromString(sorteringsRetning), feldtnavn);
    }

    static protected Sort.Order getSortingOrderForPageableBeslutter(String order, String direction) {
        SortingDirection sortingDirection = SortingDirection.valueOf(direction.toUpperCase());
        return switch (sortingDirection) {
            case ASC -> getSortingOrderForPageableASC(SortingOrder.valueOf(order.toUpperCase()));
            case DESC -> getSortingOrderForPageableDESC(SortingOrder.valueOf(order.toUpperCase()));
        };
    }

    static private Sort.Order getSortingOrderForPageableASC(SortingOrder sortingOrder) {
        return switch (sortingOrder) {
            case OPPRETTETTIDSPUNKT -> Sort.Order.asc("opprettetTidspunkt");
            case BEDRIFTNAVN -> Sort.Order.asc("bedriftNavn");
            case DELTAKERFORNAVN -> Sort.Order.asc("deltakerFornavn");
            case STATUS -> Sort.Order.asc("antallUbehandlet");
            case STARTDATO -> Sort.Order.asc("startDato");
            case SISTENDRET -> Sort.Order.asc("sistEndret");
            case TILTAKSTYPE -> Sort.Order.asc("tiltakstype");
        };
    }

    static private Sort.Order getSortingOrderForPageableDESC(SortingOrder sortingOrder) {
        return switch (sortingOrder) {
            case OPPRETTETTIDSPUNKT -> Sort.Order.desc("opprettetTidspunkt");
            case BEDRIFTNAVN -> Sort.Order.desc("bedriftNavn");
            case DELTAKERFORNAVN -> Sort.Order.desc("deltakerFornavn");
            case STATUS -> Sort.Order.desc("antallUbehandlet");
            case STARTDATO -> Sort.Order.desc("startDato");
            case SISTENDRET -> Sort.Order.desc("sistEndret");
            case TILTAKSTYPE -> Sort.Order.desc("tiltakstype");
        };
    }
}
