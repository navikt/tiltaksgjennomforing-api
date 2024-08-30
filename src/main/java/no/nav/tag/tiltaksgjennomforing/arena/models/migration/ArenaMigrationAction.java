package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.avtale.AnnullertGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

import java.util.List;

public enum ArenaMigrationAction {
    OPPRETT,
    OPPDATER,
    IGNORER,
    ANNULLER,
    AVSLUTT,
    IKKE_MULIG;

    private static final List<String> GYLDIGE_ANNULERT_GRUNNER = List.of(
        AnnullertGrunn.FEILREGISTRERING,
        AnnullertGrunn.BEGYNT_I_ARBEID,
        AnnullertGrunn.FÅTT_TILBUD_OM_ANNET_TILTAK,
        AnnullertGrunn.SYK,
        AnnullertGrunn.IKKE_MØTT
    );

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate) {
        return switch (agreementAggregate.getTiltakstatuskode()) {
            case GJENNOMFOR -> OPPRETT;
            case PLANLAGT ->
                switch (agreementAggregate.getDeltakerstatuskode()) {
                    case GJENN, AKTUELL, TILBUD -> OPPRETT;
                    default -> IKKE_MULIG;
            };
            default -> IGNORER;
        };
    }

    public static ArenaMigrationAction map(ArenaAgreementAggregate agreementAggregate, Avtale avtale) {
        boolean isFeilregistrert = avtale.isFeilregistrert();
        boolean isAnnullertWithStatusAnnet = avtale.getAnnullertTidspunkt() != null
            && !GYLDIGE_ANNULERT_GRUNNER.contains(avtale.getAnnullertGrunn());

        return switch (agreementAggregate.getTiltakstatuskode()) {
            case GJENNOMFOR -> isFeilregistrert || isAnnullertWithStatusAnnet ? OPPRETT : OPPDATER;
            case AVSLUTT, AVBRUTT -> AVSLUTT;
            case AVLYST -> ANNULLER;
            case PLANLAGT ->
                switch (agreementAggregate.getDeltakerstatuskode()) {
                    case GJENN, FULLF, AKTUELL, TILBUD -> isFeilregistrert || isAnnullertWithStatusAnnet
                        ? OPPRETT
                        : OPPDATER;
                    default -> IKKE_MULIG;
            };
        };
    }
}
