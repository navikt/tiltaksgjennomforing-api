package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;

import java.time.LocalDate;

public enum ArenaMigrationAction {
    OPPRETT,
    OPPDATER,
    IGNORER,
    ANNULLER,
    TBD,
    AVSLUTT;

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        boolean isSluttdatoInTheFuture = agreementAggregate.findSluttdato()
            .map(sluttdato -> sluttdato.isAfter(LocalDate.now())).orElse(false);

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> isSluttdatoInTheFuture ? OPPRETT : IGNORER;
            default -> IGNORER;
        };
    }

    public static ArenaMigrationAction map(
        Avtale avtale,
        ArenaAgreementAggregate agreementAggregate
    ) {
        Status avtalestatus = avtale.statusSomEnum();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        boolean isFeilregistrert = avtale.isFeilregistrert();
        boolean isSluttdatoIFremtiden = agreementAggregate.findSluttdato()
            .map(sluttdato -> sluttdato.isAfter(LocalDate.now())).orElse(false);

        return switch (avtalestatus) {
            case ANNULLERT, AVBRUTT -> switch (deltakerstatuskode) {
                case GJENN, TILBUD -> isSluttdatoIFremtiden ? (isFeilregistrert ? OPPRETT : OPPDATER) : IGNORER;
                default -> IGNORER;
            };
            case AVSLUTTET -> switch (deltakerstatuskode) {
                case GJENN, TILBUD -> isSluttdatoIFremtiden ? OPPDATER : IGNORER;
                default -> IGNORER;
            };
            case KLAR_FOR_OPPSTART -> switch(deltakerstatuskode) {
                case GJENN, TILBUD -> OPPDATER;
                default -> ANNULLER;
            };
            case GJENNOMFØRES -> switch (tiltakstatuskode) {
                case AVBRUTT -> switch(deltakerstatuskode) {
                    case GJENN_AVB -> AVSLUTT;
                    default -> ANNULLER;
                };
                case AVLYST -> ANNULLER;
                case AVSLUTT -> switch(deltakerstatuskode) {
                    case FULLF -> AVSLUTT;
                    case GJENN, TILBUD -> isSluttdatoIFremtiden ? OPPDATER : AVSLUTT;
                    default -> ANNULLER;
                };
                case GJENNOMFOR -> switch(deltakerstatuskode) {
                    case FULLF -> AVSLUTT;
                    case GJENN, TILBUD -> OPPDATER;
                    default -> ANNULLER;
                };
                default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
        };

    }

    private static String formatExceptionMsg(Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode" + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena";
    }
}
