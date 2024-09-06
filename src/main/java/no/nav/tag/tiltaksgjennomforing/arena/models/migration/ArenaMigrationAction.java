package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.AnnullertGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

import java.util.List;

public enum ArenaMigrationAction {
    CREATE,
    UPDATE,
    IGNORE,
    TERMINATE,
    END;

    private static final List<String> ANNULERT_GRUNNER_UTEN_ANNET_OG_FEILREGISTRERT = List.of(
        AnnullertGrunn.BEGYNT_I_ARBEID,
        AnnullertGrunn.FÅTT_TILBUD_OM_ANNET_TILTAK,
        AnnullertGrunn.SYK,
        AnnullertGrunn.IKKE_MØTT
    );

    public static ArenaMigrationAction map(
        Tiltakstatuskode tiltakstatuskode,
        Deltakerstatuskode deltakerstatuskode
    ) {
        return switch (tiltakstatuskode) {
            case AVBRUTT -> switch (deltakerstatuskode) {
                case AKTUELL, TILBUD -> CREATE;
                case DELAVB, FULLF, GJENN_AVB, IKKAKTUELL, IKKEM -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case AVLYST -> switch (deltakerstatuskode) {
                case FULLF, AKTUELL, GJENN_AVL, IKKAKTUELL, IKKEM -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case AVSLUTT -> switch (deltakerstatuskode) {
                case AKTUELL, GJENN, TILBUD -> CREATE;
                case FULLF, GJENN_AVB, IKKAKTUELL, IKKEM -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case GJENNOMFOR, PLANLAGT -> switch (deltakerstatuskode) {
                case AKTUELL, GJENN, TILBUD -> CREATE;
                case FULLF, IKKAKTUELL, IKKEM -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
        };
    }

    public static ArenaMigrationAction map(
        Avtale avtale,
        Tiltakstatuskode tiltakstatuskode,
        Deltakerstatuskode deltakerstatuskode
    ) {
        boolean isFeilregistrertEllerAnnullertMedAnnetSomGrunn = avtale.getAnnullertGrunn() != null &&
            !ANNULERT_GRUNNER_UTEN_ANNET_OG_FEILREGISTRERT.contains(avtale.getAnnullertGrunn());

        return switch (tiltakstatuskode) {
            case AVBRUTT -> switch (deltakerstatuskode) {
                case AKTUELL, TILBUD -> isFeilregistrertEllerAnnullertMedAnnetSomGrunn ? CREATE : UPDATE;
                case DELAVB, FULLF, GJENN_AVB -> END;
                case IKKAKTUELL, IKKEM -> TERMINATE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case AVLYST -> switch (deltakerstatuskode) {
                case FULLF -> END;
                case AKTUELL, GJENN_AVL, IKKAKTUELL, IKKEM -> TERMINATE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case AVSLUTT -> switch (deltakerstatuskode) {
                case AKTUELL, GJENN, TILBUD -> isFeilregistrertEllerAnnullertMedAnnetSomGrunn ? CREATE : UPDATE;
                case FULLF, GJENN_AVB -> END;
                case IKKAKTUELL, IKKEM -> TERMINATE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case GJENNOMFOR, PLANLAGT -> switch (deltakerstatuskode) {
                case AKTUELL, GJENN, TILBUD -> isFeilregistrertEllerAnnullertMedAnnetSomGrunn ? CREATE : UPDATE;
                case FULLF -> END;
                case IKKAKTUELL, IKKEM -> TERMINATE;
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
        };
    }

    private static String formatExceptionMsg(Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode" + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena";
    }
}
