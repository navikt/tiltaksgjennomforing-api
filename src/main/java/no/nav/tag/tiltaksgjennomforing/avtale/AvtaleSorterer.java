package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class AvtaleSorterer {
    public List<Avtale> sorterAvtaler(String sorteringskolonne, List<Avtale> avtaler) {
        return avtaler.stream().sorted(comparatorForAvtale(sorteringskolonne)).collect(Collectors.toList());
    }

    private Comparator<Avtale> comparatorForAvtale(String sorteringskolonne) {
        Function<Avtale, Comparable> feltFunction;
        switch (sorteringskolonne) {
            case Avtale.Fields.sistEndret:
                feltFunction = Avtale::getSistEndret;
                break;
            case Avtale.Fields.opprettetTidspunkt:
                feltFunction = Avtale::getOpprettetTidspunkt;
                break;
            case AvtaleInnhold.Fields.bedriftNavn:
                feltFunction = Avtale::getBedriftNavn;
                break;
            case AvtaleInnhold.Fields.deltakerEtternavn:
                feltFunction = Avtale::getDeltakerEtternavn;
                break;
            case AvtaleInnhold.Fields.deltakerFornavn:
                feltFunction = Avtale::getDeltakerFornavn;
                break;
            case "status":
                feltFunction = Avtale::status;
                break;
            default:
                feltFunction = Avtale::getSistEndret;
        }
        return Comparator.comparing(feltFunction, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
