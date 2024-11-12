package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;

import java.time.LocalDate;

public enum ArenaMigrationAction {
    GJENOPPRETT,
    OPPRETT,
    OPPDATER,
    IGNORER,
    ANNULLER,
    AVSLUTT;

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        boolean isSluttdatoInTheFuture = agreementAggregate.findSluttdato()
            .map(sluttdato -> sluttdato.isAfter(LocalDate.now())).orElse(false);

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> isSluttdatoInTheFuture ? OPPRETT : IGNORER;
            case null, default -> IGNORER;
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
                case GJENN, TILBUD -> isSluttdatoIFremtiden ? (isFeilregistrert ? OPPRETT : GJENOPPRETT) : IGNORER;
                case null, default -> IGNORER;
            };
            case AVSLUTTET -> switch (deltakerstatuskode) {
                case GJENN, TILBUD -> isSluttdatoIFremtiden ? GJENOPPRETT : IGNORER;
                case null, default -> IGNORER;
            };
            case KLAR_FOR_OPPSTART -> switch(deltakerstatuskode) {
                case GJENN, TILBUD -> OPPDATER;
                case null, default -> ANNULLER;
            };
            case GJENNOMFØRES -> switch (tiltakstatuskode) {
                case AVBRUTT -> switch(deltakerstatuskode) {
                    case GJENN_AVB -> AVSLUTT;
                    case null, default -> ANNULLER;
                };
                case AVLYST -> ANNULLER;
                case AVSLUTT -> switch(deltakerstatuskode) {
                    case FULLF -> AVSLUTT;
                    case GJENN, TILBUD -> isSluttdatoIFremtiden ? OPPDATER : AVSLUTT;
                    case null, default -> ANNULLER;
                };
                case GJENNOMFOR -> switch(deltakerstatuskode) {
                    case FULLF -> AVSLUTT;
                    case GJENN, TILBUD -> OPPDATER;
                    case null, default -> ANNULLER;
                };
                case null, default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
            };
            case null, default -> throw new IllegalStateException(formatExceptionMsg(tiltakstatuskode, deltakerstatuskode));
        };

    }

    private static String formatExceptionMsg(Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode" + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena";
    }
}
