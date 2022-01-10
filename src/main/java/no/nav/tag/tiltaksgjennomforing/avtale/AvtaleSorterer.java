package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

import java.util.Comparator;

@UtilityClass
public class AvtaleSorterer {
    public Comparator<Avtale> comparatorForAvtale(String sorteringskolonne) {
        return switch (sorteringskolonne) {
            case Avtale.Fields.opprettetTidspunkt -> Comparator.comparing(Avtale::getOpprettetTidspunkt, Comparator.reverseOrder());
            case AvtaleInnhold.Fields.bedriftNavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getBedriftNavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerEtternavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getDeltakerEtternavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerFornavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getDeltakerFornavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(Avtale::status);
            default -> Comparator.comparing(Avtale::getSistEndret, Comparator.reverseOrder());
        };
    }

    private static String lowercaseEllerNull(String x) {
        return x != null ? x.toLowerCase() : null;
    }
}
