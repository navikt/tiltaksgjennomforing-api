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

    private static final List<String> GYLDIGE_ANNULERT_GRUNNER = List.of(
        AnnullertGrunn.FEILREGISTRERING,
        AnnullertGrunn.BEGYNT_I_ARBEID,
        AnnullertGrunn.FÅTT_TILBUD_OM_ANNET_TILTAK,
        AnnullertGrunn.SYK,
        AnnullertGrunn.IKKE_MØTT
    );

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        return switch (agreementAggregate.getTiltakstatuskode()) {
            case GJENNOMFOR, PLANLAGT -> switch (agreementAggregate.getDeltakerstatuskode()) {
                case AKTUELL, FULLF, GJENN, IKKAKTUELL, IKKEM, TILBUD -> CREATE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVBRUTT -> switch (agreementAggregate.getDeltakerstatuskode()) {
                case AKTUELL, DELAVB, FULLF, GJENN_AVB, IKKAKTUELL, IKKEM, TILBUD -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVLYST -> switch (agreementAggregate.getDeltakerstatuskode()) {
                case AKTUELL, FULLF, GJENN_AVL, IKKAKTUELL, IKKEM -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVSLUTT -> switch (agreementAggregate.getDeltakerstatuskode()) {
                case AKTUELL, FULLF, GJENN, GJENN_AVB, IKKAKTUELL, IKKEM, TILBUD -> IGNORE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
        };
    }

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate, Avtale avtale) {
        boolean isFeilregistrert = avtale.isFeilregistrert();
        boolean isAnnullert = avtale.getAnnullertTidspunkt() != null &&
            !GYLDIGE_ANNULERT_GRUNNER.contains(avtale.getAnnullertGrunn());
        boolean isFeilregOrAnnullert = isFeilregistrert || isAnnullert;

        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();

        return switch (tiltakstatuskode) {
            case GJENNOMFOR, PLANLAGT -> switch (deltakerstatuskode) {
                case AKTUELL, FULLF, GJENN, IKKAKTUELL, IKKEM, TILBUD -> isFeilregOrAnnullert ? CREATE : UPDATE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVBRUTT -> switch (deltakerstatuskode) {
                case AKTUELL, DELAVB, FULLF, GJENN_AVB, IKKAKTUELL, IKKEM, TILBUD -> END;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVLYST -> switch (deltakerstatuskode) {
                case AKTUELL, FULLF, GJENN_AVL, IKKAKTUELL, IKKEM -> TERMINATE;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
            case AVSLUTT -> switch (deltakerstatuskode) {
                case AKTUELL, FULLF, GJENN, GJENN_AVB, IKKAKTUELL, IKKEM, TILBUD -> END;
                default -> throw new IllegalStateException(formatExceptionMsg(agreementAggregate));
            };
        };
    }

    private static String formatExceptionMsg(ArenaAgreementAggregate agreementAggregate) {
        return "Fikk ugyldig kombinasjon av tiltakstatuskode" + agreementAggregate.getTiltakstatuskode() +
               " og deltakerstatuskode" + agreementAggregate.getDeltakerstatuskode() + " fra Arena";
    }
}
