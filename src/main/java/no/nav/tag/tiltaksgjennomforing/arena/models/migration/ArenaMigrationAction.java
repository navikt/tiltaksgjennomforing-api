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

        if (agreementAggregate.isDublett()) {
            return IGNORER;
        }

        boolean isSluttdatoIAarEllerFremtiden = agreementAggregate.isSluttdatoIAarEllerFremtiden();

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> OPPRETT;
            case FULLF, GJENN_AVB -> isSluttdatoIAarEllerFremtiden ? OPPRETT : IGNORER;
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
        boolean isSluttdatoIAarEllerFremtiden = agreementAggregate.isSluttdatoIAarEllerFremtiden();

        if (agreementAggregate.isDublett()) {
            return IGNORER;
        }

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> switch (avtalestatus) {
                case ANNULLERT -> (isFeilregistrert ? OPPRETT : OPPDATER);
                case null ->  throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
                default -> OPPDATER;
            };
            case FULLF, GJENN_AVB -> switch (avtalestatus) {
                case ANNULLERT -> isSluttdatoIAarEllerFremtiden ? (isFeilregistrert ? OPPRETT : OPPDATER) : IGNORER;
                case AVSLUTTET -> isSluttdatoIAarEllerFremtiden ? OPPDATER : IGNORER;
                case null ->  throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
                default -> isSluttdatoIAarEllerFremtiden ? OPPDATER : AVSLUTT;
            };
            case null -> throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
            default -> ANNULLER;
        };
    }

    private static String formatExceptionMsg(Status status, Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode " + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena på avtale med status " + status;
    }
}
