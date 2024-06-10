package no.nav.tag.tiltaksgjennomforing.avtale;

public record VtaoFelter(
        String fadderFornavn,
        String fadderEtternavn,
        String fadderTlf
) {
    public VtaoFelter(Vtao vtao) {
        this(vtao.getFadderFornavn(), vtao.getFadderEtternavn(), vtao.getFadderTlf());
    }
}
