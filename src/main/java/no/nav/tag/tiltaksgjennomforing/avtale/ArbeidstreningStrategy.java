package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreAvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.MaalDTO;

import java.util.HashMap;
import java.util.Map;

public class ArbeidstreningStrategy extends BaseAvtaleInnholdStrategy {

    public ArbeidstreningStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtaleDTO nyAvtale) {
        nyAvtale.getMaal().forEach(MaalDTO::sjekkMaalLengde);
        avtaleInnhold.getMaal().clear();
        avtaleInnhold.getMaal().addAll(Maal.map(nyAvtale.getMaal()));
        avtaleInnhold.getMaal().forEach(m -> m.setAvtaleInnhold(avtaleInnhold));
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        avtaleInnhold.setRefusjonKontaktperson(nyAvtale.getRefusjonKontaktperson());
        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        HashMap<String, Object> alleFelterSomMåFyllesUt = new HashMap<>();
        alleFelterSomMåFyllesUt.putAll(super.alleFelterSomMåFyllesUt());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.stillingprosent, avtaleInnhold.getStillingprosent());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.stillingstittel, avtaleInnhold.getStillingstittel());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.arbeidsoppgaver, avtaleInnhold.getArbeidsoppgaver());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.maal, avtaleInnhold.getMaal());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.antallDagerPerUke, avtaleInnhold.getAntallDagerPerUke());
        return alleFelterSomMåFyllesUt;
    }
}
