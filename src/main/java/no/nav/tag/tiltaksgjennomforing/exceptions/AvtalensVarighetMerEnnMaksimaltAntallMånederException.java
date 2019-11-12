package no.nav.tag.tiltaksgjennomforing.exceptions;

public class AvtalensVarighetMerEnnMaksimaltAntallMånederException extends TiltaksgjennomforingException {
    public AvtalensVarighetMerEnnMaksimaltAntallMånederException(int antallMåneder) {
        super(String.format("Avtalens varighet er mer enn %s måneder", antallMåneder));
    }
}
