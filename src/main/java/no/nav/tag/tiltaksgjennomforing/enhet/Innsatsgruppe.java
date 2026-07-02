package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Innsatsgruppe {
    GODE_MULIGHETER,
    TRENGER_VEILEDNING,
    TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE,
    JOBBE_DELVIS,
    LITEN_MULIGHET_TIL_A_JOBBE,
    UKJENT;

    public boolean erGyldig() {
        return switch (this) {
            case GODE_MULIGHETER, UKJENT -> false;
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, TRENGER_VEILEDNING, LITEN_MULIGHET_TIL_A_JOBBE,
                 JOBBE_DELVIS -> true;
        };
    }

    public boolean erKvalifisererTilMidlertidiglonnstilskuddOgSommerjobbOgMentor() {
        return switch (this) {
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, TRENGER_VEILEDNING, LITEN_MULIGHET_TIL_A_JOBBE,
                 JOBBE_DELVIS -> true;
            default -> false;
        };
    }

    public boolean erKvalifisererTilFirearigLonnstilskuddForUnge() {
        return switch (this) {
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> true;
            default -> false;
        };
    }

    public boolean erKvalifisererTilVariglonnstilskudd() {
        return switch (this) {
            case LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> true;
            default -> false;
        };
    }

    public boolean erKvalifisererTilVtao(){
        return switch (this) {
            case LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> true;
            default -> false;
        };
    }

    public Integer finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(Integer prosentsatsLiten, Integer prosentsatsStor) {
        return switch (this) {
            case TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE, LITEN_MULIGHET_TIL_A_JOBBE, JOBBE_DELVIS -> prosentsatsStor;
            case TRENGER_VEILEDNING -> prosentsatsLiten;
            default -> {
                log.warn("feilet med setting av kvalifiseringsgruppe. Kvalifiseringsgruppe: {}", this);
                yield null;
            }
        };
    }
}
