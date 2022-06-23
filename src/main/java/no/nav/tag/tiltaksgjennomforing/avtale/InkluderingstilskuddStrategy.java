package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InkluderingstilskuddStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingstilskuddStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        sjekkTotalBeløp();

        avtaleInnhold.getInkluderingstilskuddsutgift().clear();
        avtaleInnhold.getInkluderingstilskuddsutgift().addAll(nyAvtale.getInkluderingstilskuddsutgift());
        avtaleInnhold.getInkluderingstilskuddsutgift().forEach(i -> i.setAvtaleInnhold(avtaleInnhold));

        avtaleInnhold.setInkluderingstilskuddBegrunnelse(nyAvtale.getInkluderingstilskuddBegrunnelse());

        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        var alleFelter = new HashMap<String, Object>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.inkluderingstilskuddsutgift, avtaleInnhold.getInkluderingstilskuddsutgift());
        return alleFelter;
    }

    private void sjekkTotalBeløp() {
        Integer MAX_SUM = 136700;
        Integer sum = avtaleInnhold.inkluderingstilskuddTotalBeløp();
        if (sum > MAX_SUM) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_SUM_FOR_HØY);
        }
    }

}
