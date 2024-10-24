package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationAction;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndreAvtaleArena implements Comparable<Avtale> {
    public enum Handling {
        OPPDATER,
        AVSLUTT,
        ANNULLER;

        public static Handling map(ArenaMigrationAction arenaMigrationAction) {
            return switch (arenaMigrationAction) {
                case OPPDATER -> OPPDATER;
                case ANNULLER -> ANNULLER;
                case AVSLUTT -> AVSLUTT;
                default -> throw new IllegalArgumentException("Ugyldig handling: " + arenaMigrationAction);
            };
        }
    }

    private LocalDate startdato;
    private LocalDate sluttdato;
    private Integer stillingprosent;
    private Integer antallDagerPerUke;
    private Handling handling;

    @Override
    public int compareTo(@NotNull Avtale a) {
        return compareToNullSafe(this.startdato, a.getGjeldendeInnhold().getStartDato())
            .or(() -> compareToNullSafe(this.sluttdato, a.getGjeldendeInnhold().getSluttDato()))
            .or(() -> compareToNullSafe(this.stillingprosent, a.getGjeldendeInnhold().getStillingprosent()))
            .or(() -> compareToNullSafe(this.antallDagerPerUke, a.getGjeldendeInnhold().getAntallDagerPerUke()))
            .orElse(0);
    }

    private static <T extends Comparable<T>> Optional<Integer> compareToNullSafe(T a, T b) {
        if (a == null && b == null) {
            return Optional.empty();
        }
        if (a == null || b == null) {
            return Optional.of(a == null ? -1 : 1);
        }
        int result = a.compareTo(b);
        return result == 0 ? Optional.empty() : Optional.of(result);
    }
}
