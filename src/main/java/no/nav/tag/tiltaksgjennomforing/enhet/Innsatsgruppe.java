package no.nav.tag.tiltaksgjennomforing.enhet;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.Set;

public enum Innsatsgruppe {
    GODE_MULIGHETER,
    TRENGER_VEILEDNING,
    TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE,
    JOBBE_DELVIS,
    LITEN_MULIGHET_TIL_A_JOBBE,
    UKJENT;

    private static final Set<Innsatsgruppe> VARIG_TILPASSET_VARIANTER = Set.of(JOBBE_DELVIS, LITEN_MULIGHET_TIL_A_JOBBE);

    public static boolean isArenaOboEqual(Innsatsgruppe innsatsgruppeArena, Innsatsgruppe innsatsgruppeObo) {
        if (innsatsgruppeObo == UKJENT) {
            return false;
        }
        if (innsatsgruppeArena == innsatsgruppeObo) {
            return true;
        }
        if (innsatsgruppeArena == UKJENT && innsatsgruppeObo == null) {
            return true;
        }
        return innsatsgruppeArena != null && innsatsgruppeObo != null
            && VARIG_TILPASSET_VARIANTER.contains(innsatsgruppeArena)
            && VARIG_TILPASSET_VARIANTER.contains(innsatsgruppeObo);
    }

    public boolean erGyldig(Tiltakstype tiltakstype) {
        return switch (tiltakstype) {
            case ARBEIDSTRENING, INKLUDERINGSTILSKUDD, MIDLERTIDIG_LONNSTILSKUDD, SOMMERJOBB, MENTOR -> switch (this) {
                case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE,
                     TRENGER_VEILEDNING,
                     LITEN_MULIGHET_TIL_A_JOBBE,
                     JOBBE_DELVIS -> true;
                default -> false;
            };
            case FIREARIG_LONNSTILSKUDD -> switch (this) {
                case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> true;
                default -> false;
            };
            case VARIG_LONNSTILSKUDD, VTAO -> switch (this) {
                case LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> true;
                default -> false;
            };
        };
    }
}
