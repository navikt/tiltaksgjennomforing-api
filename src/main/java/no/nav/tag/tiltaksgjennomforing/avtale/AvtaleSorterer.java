package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.Comparator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AvtaleSorterer {
    public Comparator<Avtale> comparatorForAvtale(String sorteringskolonne) {
        return switch (sorteringskolonne) {
            case Avtale.Fields.opprettetTidspunkt -> Comparator.comparing(Avtale::getOpprettetTidspunkt, Comparator.reverseOrder());
            case AvtaleInnhold.Fields.bedriftNavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getBedriftNavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerEtternavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getDeltakerEtternavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case AvtaleInnhold.Fields.deltakerFornavn -> Comparator.comparing(avtale -> lowercaseEllerNull(avtale.getGjeldendeInnhold().getDeltakerFornavn()), Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(Avtale::status);
            case "startDato" -> Comparator.comparing(avtale -> avtale.gjeldendeTilskuddsperiode().getStartDato(), Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Avtale::getSistEndret, Comparator.reverseOrder());
        };
    }

    private static String lowercaseEllerNull(String x) {
        return x != null ? x.toLowerCase() : null;
    }
}
