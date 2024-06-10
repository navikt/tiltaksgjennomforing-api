package no.nav.tag.tiltaksgjennomforing.avtale;

public class VtaoStrategy extends BaseAvtaleInnholdStrategy {
    public VtaoStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        if (nyAvtale.getVtao() != null) {
            var eksisterendeVtao = avtaleInnhold.getVtao();
            if (eksisterendeVtao != null) {
                eksisterendeVtao.setFadderFornavn(nyAvtale.getVtao().fadderFornavn());
                eksisterendeVtao.setFadderEtternavn(nyAvtale.getVtao().fadderEtternavn());
                eksisterendeVtao.setFadderTlf(nyAvtale.getVtao().fadderTlf());
            } else {
                avtaleInnhold.setVtao(new Vtao(nyAvtale.getVtao(), avtaleInnhold));
            }
        }
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());

        super.endre(nyAvtale);
    }
}
