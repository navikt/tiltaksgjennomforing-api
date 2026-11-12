package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

public record Oppfolgingsenhet(String id, String navn, String kilde) {
    private static final String KILDE_ARENA = "ARENA";

    public boolean erFraArena() {
        return KILDE_ARENA.equals(kilde);
    }
}
