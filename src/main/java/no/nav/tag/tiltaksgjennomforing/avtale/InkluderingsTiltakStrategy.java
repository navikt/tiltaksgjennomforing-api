package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.HashMap;
import java.util.Map;

public class InkluderingsTiltakStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingsTiltakStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        avtaleInnhold.getInkluderingstilskudd().clear();
        avtaleInnhold.getInkluderingstilskudd().addAll(nyAvtale.getInkluderingstilskudd());
        avtaleInnhold.getInkluderingstilskudd().forEach(i -> i.setAvtaleInnhold(avtaleInnhold));
        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        var alleFelter = new HashMap<String, Object>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.inkluderingstilskudd, avtaleInnhold.getInkluderingstilskudd());
        return alleFelter;
    }

}
