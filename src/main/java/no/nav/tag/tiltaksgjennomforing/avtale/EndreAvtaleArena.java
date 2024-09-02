package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationAction;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndreAvtaleArena {
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
}
