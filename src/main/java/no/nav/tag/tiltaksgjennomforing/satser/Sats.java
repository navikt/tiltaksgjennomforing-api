package no.nav.tag.tiltaksgjennomforing.satser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Sats {
    private final String typeNavn;
    private final NavigableMap<LocalDate, Double> satsePerioder;

    Sats(String typenavn, List<SatserEntitet> satserEntitet) {
        this.typeNavn = typenavn;
        this.satsePerioder = lagPeriodesett(satserEntitet);
    }

    public String getTypeNavn() {
        return typeNavn;
    }

    public NavigableMap<LocalDate, Double> getSatsePerioder() {
        return satsePerioder;
    }

    public Double hentGjeldendeSats(LocalDate dato) {
        var entry = satsePerioder.floorEntry(dato);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public String toString() {
        var string = new StringBuilder("Sats: " + typeNavn + "\n");
        var satseperiodeEntryset = new ArrayList<>(this.satsePerioder.entrySet());
        for (int i = 0; i < satseperiodeEntryset.size() - 1; i++) {
            string.append(satseperiodeEntryset.get(i).getKey());
            string.append("\t");
            string.append(satseperiodeEntryset.get(i + 1).getKey().minusDays(1));
            string.append(":\t");
            string.append(satseperiodeEntryset.get(i).getValue());
            string.append("\n");
        }
        if (!satseperiodeEntryset.isEmpty()) {
            var sistePeriode = satseperiodeEntryset.getLast();
            string.append(sistePeriode.getKey());
            string.append("\t");
            string.append(LocalDate.MAX);
            string.append(":\t");
            string.append(sistePeriode.getValue());
            string.append("\n");
        }
        return string.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sats sats = (Sats) o;
        return typeNavn.equals(sats.typeNavn) && satsePerioder.equals(sats.satsePerioder);
    }

    @Override
    public int hashCode() {
        int result = typeNavn.hashCode();
        result = 31 * result + satsePerioder.hashCode();
        return result;
    }

    static NavigableMap<LocalDate, Double> lagPeriodesett(List<SatserEntitet> satserEntitet) {
        NavigableMap<LocalDate, Double> m = new TreeMap<>();

        satserEntitet.stream()
                .sorted(Comparator.comparing(SatserEntitet::getGyldigFraOgMed))
                .forEach(sats -> {
                    m.put(sats.getGyldigFraOgMed(), sats.getSatsVerdi());
                    if (sats.getGyldigTilOgMed() != null) {
                        m.put(sats.getGyldigTilOgMed().plusDays(1), null);
                    }
                });

        return m;
    }
}
