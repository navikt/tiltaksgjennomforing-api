package no.nav.tag.tiltaksgjennomforing.avtale;

public class VtaoStrategy extends BaseAvtaleInnholdStrategy {
    public VtaoStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        if (nyAvtale.getVtao() != null) {
            var eksisterendeVtao = avtaleInnhold.getVtao();
            var nyVtao = new Vtao(nyAvtale.getVtao(), avtaleInnhold);
            if (eksisterendeVtao == null) {
                avtaleInnhold.setVtao(nyVtao);
            } else {
                eksisterendeVtao.setFadderFornavn(nyVtao.getFadderFornavn());
                eksisterendeVtao.setFadderEtternavn(nyVtao.getFadderEtternavn());
                eksisterendeVtao.setFadderTlf(nyVtao.getFadderTlf());

            }
        }
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());

        super.endre(nyAvtale);}
}
