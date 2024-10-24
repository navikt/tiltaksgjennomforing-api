package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

public record HentOppfolgingsstatusRespons(
    Oppfolgingsenhet oppfolgingsenhet,
    String veilederId,
    String formidlingsgruppe,
    String servicegruppe,
    String hovedmaalkode
) {
    public record Oppfolgingsenhet(String navn, String enhetId) {}
}
