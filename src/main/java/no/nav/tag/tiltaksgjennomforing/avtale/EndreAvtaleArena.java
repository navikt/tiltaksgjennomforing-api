package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationAction;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

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
                case UPDATE -> OPPDATER;
                case TERMINATE -> ANNULLER;
                case END -> AVSLUTT;
                default -> throw new IllegalArgumentException("Ugyldig handling: " + arenaMigrationAction);
            };
        }
    }

    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer stillingprosent;
    private Integer antallDagerPerUke;
    private Handling handling;

    @Override
    public int compareTo(@NotNull Avtale a) {
        if (!startDato.isEqual(a.getGjeldendeInnhold().getStartDato())) {
            return startDato.compareTo(a.getGjeldendeInnhold().getStartDato());
        }
        if (!sluttDato.isEqual(a.getGjeldendeInnhold().getSluttDato())) {
            return sluttDato.compareTo(a.getGjeldendeInnhold().getSluttDato());
        }
        if (!stillingprosent.equals(a.getGjeldendeInnhold().getStillingprosent())) {
            return stillingprosent.compareTo(a.getGjeldendeInnhold().getStillingprosent());
        }
        return antallDagerPerUke.compareTo(a.getGjeldendeInnhold().getAntallDagerPerUke());
    }
}
