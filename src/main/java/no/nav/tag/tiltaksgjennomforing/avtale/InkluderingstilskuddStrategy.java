package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.HashMap;
import java.util.Map;

public class InkluderingstilskuddStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        nyAvtale.getMaal().forEach(Maal::sjekkMaalLengde);
        avtaleInnhold.getInkluderingstilskudd().clear();
        avtaleInnhold.getInkluderingstilskudd().addAll(nyAvtale.getInkluderingstilskudd());
        avtaleInnhold.getInkluderingstilskudd().forEach(i -> i.setAvtaleInnhold(avtaleInnhold));
        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        HashMap<String, Object> alleFelterSomMåFyllesUt = new HashMap<>();
        alleFelterSomMåFyllesUt.putAll(super.alleFelterSomMåFyllesUt());
        alleFelterSomMåFyllesUt.put(AvtaleInnhold.Fields.inkluderingstilskudd, avtaleInnhold.getInkluderingstilskudd());
        return alleFelterSomMåFyllesUt;
    }
}
