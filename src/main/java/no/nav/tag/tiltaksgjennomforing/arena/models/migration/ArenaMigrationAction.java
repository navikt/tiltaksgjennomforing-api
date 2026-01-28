package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;

import java.util.List;

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

        boolean isSluttdatoEtterMigreringAvTilskudd = agreementAggregate.isSluttdatoPaaEllerEtter(
            ArenaTiltakskode.GJELDENDE_MIGRERING.getMigreringsdatoForTilskudd()
        );

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> OPPRETT;
            case FULLF, GJENN_AVB -> isSluttdatoEtterMigreringAvTilskudd ? OPPRETT : IGNORER;
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
        boolean isSluttdatoEtterMigreringAvTilskudd = agreementAggregate.isSluttdatoPaaEllerEtter(
            ArenaTiltakskode.GJELDENDE_MIGRERING.getMigreringsdatoForTilskudd()
        );

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
                case ANNULLERT -> isSluttdatoEtterMigreringAvTilskudd ? (isFeilregistrert ? OPPRETT : OPPDATER) : IGNORER;
                case AVSLUTTET -> isSluttdatoEtterMigreringAvTilskudd ? OPPDATER : IGNORER;
                case null ->  throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
                default -> isSluttdatoEtterMigreringAvTilskudd ? OPPDATER : AVSLUTT;
            };
            case null -> switch (tiltakstatuskode) {
                case AVBRUTT, AVSLUTT -> List.of(Status.AVSLUTTET, Status.ANNULLERT).contains(avtalestatus) ? IGNORER : AVSLUTT;
                case AVLYST -> List.of(Status.AVSLUTTET, Status.ANNULLERT).contains(avtalestatus) ? IGNORER : ANNULLER;
                case null ->  throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
                default -> ANNULLER;
            };
            default -> switch (avtalestatus) {
                case ANNULLERT, AVSLUTTET -> IGNORER;
                case null -> throw new IllegalStateException(formatExceptionMsg(avtalestatus, tiltakstatuskode, deltakerstatuskode));
                default -> ANNULLER;
            };
        };
    }

    private static String formatExceptionMsg(Status status, Tiltakstatuskode tiltakstatuskode, Deltakerstatuskode deltakerstatuskode) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode " + tiltakstatuskode + " og deltakerstatuskode " +
            deltakerstatuskode + " fra Arena på avtale med status " + status;
    }
}
