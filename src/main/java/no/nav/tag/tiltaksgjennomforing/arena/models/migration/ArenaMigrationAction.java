package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;

public enum ArenaMigrationAction {
    GJENOPPRETT,
    OPPRETT,
    OPPDATER,
    IGNORER,
    ANNULLER,
    AVSLUTT;

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        boolean isSluttdatoIDagEllerFremtiden = agreementAggregate.isSluttdatoIDagEllerFremtiden();

        if (agreementAggregate.isDublett()) {
            return IGNORER;
        }

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> isSluttdatoIDagEllerFremtiden ? OPPRETT : IGNORER;
            case null, default -> IGNORER;
        };
    }

    public static ArenaMigrationAction map(
        Avtale avtale,
        ArenaAgreementAggregate agreementAggregate
    ) {
        Status avtalestatus = avtale.getStatus();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        boolean isFeilregistrert = avtale.isFeilregistrert();
        boolean isSluttdatoIDagEllerFremtiden = agreementAggregate.isSluttdatoIDagEllerFremtiden();

        if (agreementAggregate.isDublett()) {
            return IGNORER;
        }

        return switch (avtalestatus) {
            case ANNULLERT -> switch (deltakerstatuskode) {
                case GJENN, TILBUD -> isSluttdatoIDagEllerFremtiden ? (isFeilregistrert ? OPPRETT : GJENOPPRETT) : IGNORER;
                case null, default -> IGNORER;
            };
            case AVSLUTTET -> switch (deltakerstatuskode) {
                case GJENN, TILBUD -> isSluttdatoIDagEllerFremtiden ? GJENOPPRETT : IGNORER;
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
                case AVSLUTT, GJENNOMFOR -> switch(deltakerstatuskode) {
                    case FULLF -> AVSLUTT;
                    case GJENN, TILBUD -> isSluttdatoIDagEllerFremtiden ? OPPDATER : AVSLUTT;
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
