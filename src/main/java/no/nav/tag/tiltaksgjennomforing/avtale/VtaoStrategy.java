package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.HashMap;
import java.util.Map;

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
        avtaleInnhold.setStillingstype(nyAvtale.getStillingstype());

        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> alleFelter = super.alleFelterSomMåFyllesUt();
        if (avtaleInnhold.getVtao() != null) {
            alleFelter.put(Vtao.Fields.fadderFornavn, avtaleInnhold.getVtao().getFadderFornavn());
            alleFelter.put(Vtao.Fields.fadderEtternavn, avtaleInnhold.getVtao().getFadderEtternavn());
            alleFelter.put(Vtao.Fields.fadderTlf, avtaleInnhold.getVtao().getFadderTlf());
        }
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverKontonummer, avtaleInnhold.getArbeidsgiverKontonummer());
        return alleFelter;
    }
}
