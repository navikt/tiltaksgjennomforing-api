package no.nav.tag.tiltaksgjennomforing.exceptions;

public class AvtalensLengdeErMerEnn3MndException extends TiltaksgjennomforingException {
    public AvtalensLengdeErMerEnn3MndException() {
        super("Avtalens lengde er mer enn tre måneder");
    }
}
