package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;

import java.time.LocalDate;

public enum ArenaMigrationAction {
    CREATE,
    UPDATE,
    IGNORE,
    TERMINATE,
    TBD,
    END;

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        boolean isSluttdatoInTheFuture = agreementAggregate.findSluttdato()
            .map(sluttdato -> sluttdato.isAfter(LocalDate.now())).orElse(false);

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> isSluttdatoInTheFuture ? CREATE : IGNORE;
            default -> IGNORE;
        };
    }

    public static ArenaMigrationAction map(
        Avtale avtale,
        ArenaAgreementAggregate agreementAggregate
    ) {
        Status avtalestatus = avtale.statusSomEnum();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        boolean isSluttdatoInTheFuture = agreementAggregate.findSluttdato()
            .map(sluttdato -> sluttdato.isAfter(LocalDate.now())).orElse(false);

        return switch (avtalestatus) {
            case ANNULLERT ->
                switch (tiltakstatuskode) {
                    case AVSLUTT -> switch (deltakerstatuskode) {
                        case GJENN -> isSluttdatoInTheFuture ? TBD : IGNORE;
                        default -> IGNORE;
                    };
                    case GJENNOMFOR -> switch (deltakerstatuskode) {
                        case AKTUELL -> isSluttdatoInTheFuture ? TBD : IGNORE;
                        case GJENN -> TBD;
                        default -> IGNORE;
                    };
                    default -> IGNORE;
                };
            case AVSLUTTET -> switch (tiltakstatuskode) {
                case AVBRUTT -> switch(deltakerstatuskode) {
                    case TILBUD -> isSluttdatoInTheFuture ? IGNORE : TBD;
                    default -> IGNORE;
                };
                case AVSLUTT -> switch(deltakerstatuskode) {
                    case GJENN -> isSluttdatoInTheFuture ? TBD : IGNORE;
                    default -> IGNORE;
                };
                case GJENNOMFOR -> switch(deltakerstatuskode) {
                    case GJENN -> isSluttdatoInTheFuture ? UPDATE : TBD;
                    default -> IGNORE;
                };
                default -> IGNORE;
            };
            case GJENNOMFØRES -> switch (tiltakstatuskode) {
                case AVBRUTT -> switch(deltakerstatuskode) {
                    case AKTUELL -> TBD;
                    case GJENN_AVB -> END;
                    default -> TERMINATE;
                };
                case AVLYST -> TERMINATE;
                case AVSLUTT -> switch(deltakerstatuskode) {
                    case FULLF, TILBUD -> END;
                    case GJENN -> isSluttdatoInTheFuture ? TBD : END;
                    default -> TERMINATE;
                };
                case GJENNOMFOR -> switch(deltakerstatuskode) {
                    case AKTUELL -> TBD;
                    case FULLF -> END;
                    case GJENN, TILBUD -> UPDATE;
                    case IKKAKTUELL, IKKEM -> TERMINATE;
                    default -> TBD;
                };
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case KLAR_FOR_OPPSTART -> switch(deltakerstatuskode) {
                case GJENN, TILBUD -> UPDATE;
                default -> TBD;
            };
            default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
        };

    }

    private static String formatExceptionMsg(Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode" + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena";
    }
}
