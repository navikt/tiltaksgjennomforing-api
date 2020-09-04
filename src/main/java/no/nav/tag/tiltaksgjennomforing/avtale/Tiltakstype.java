package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.Optional;

public enum Tiltakstype {
    ARBEIDSTRENING(Optional.of(18)), MIDLERTIDIG_LONNSTILSKUDD(Optional.of(24)), VARIG_LONNSTILSKUDD(Optional.empty()), MENTOR(Optional.empty());

    private final Optional<Integer> varighet;

    Tiltakstype(Optional<Integer> varighet){
        this.varighet = varighet;
    }

    public Optional<Integer> varighet() {
        return varighet;
    }
}
