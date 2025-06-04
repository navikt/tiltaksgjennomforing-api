package no.nav.tag.tiltaksgjennomforing.arena.models.migration;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
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

        return switch (deltakerstatuskode) {
            case GJENN, TILBUD -> OPPRETT;
            case null, default -> IGNORER;
        };
    }

    public static ArenaMigrationAction map(
        Avtale avtale,
        ArenaAgreementAggregate agreementAggregate
    ) {
        Status avtalestatus = avtale.getStatus();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();
        boolean isFeilregistrert = avtale.isFeilregistrert();

        if (agreementAggregate.isDublett()) {
            return IGNORER;
        }

        return switch (avtalestatus) {
            case ANNULLERT, AVBRUTT -> switch (deltakerstatuskode) {
                case TILBUD, GJENN -> isFeilregistrert ? OPPRETT : GJENOPPRETT;
                case null, default -> IGNORER;
            };
            case AVSLUTTET -> switch (deltakerstatuskode) {
                case TILBUD, GJENN -> GJENOPPRETT;
                case FULLF, DELAVB -> AVSLUTT;
                case GJENN_AVL, FEILREG, IKKEM -> ANNULLER;
                case null, default -> OPPDATER;
            };
            case PÅBEGYNT, MANGLER_GODKJENNING, KLAR_FOR_OPPSTART, GJENNOMFØRES -> switch (deltakerstatuskode) {
                case TILBUD, GJENN -> OPPDATER;
                case FULLF, DELAVB -> AVSLUTT;
                case null, default -> ANNULLER;
            };
        };
    }
}
