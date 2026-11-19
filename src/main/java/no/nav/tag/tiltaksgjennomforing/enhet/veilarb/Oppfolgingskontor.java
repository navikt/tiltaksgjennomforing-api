package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

public record Oppfolgingskontor(String kontorId, String kontorNavn, KontorType kontorType) {

    public enum KontorType {
        ARBEIDSOPPFOLGING,
        ARENA,
        GEOGRAFISK_TILKNYTNING
    }

    public boolean erGyldigOppfolgingskontor() {
        return kontorType == KontorType.ARBEIDSOPPFOLGING;
    }
}
