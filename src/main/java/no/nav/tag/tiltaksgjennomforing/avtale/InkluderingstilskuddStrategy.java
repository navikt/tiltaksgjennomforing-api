package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.INKLUDERINGSTILSKUDD_SATS;

public class InkluderingstilskuddStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingstilskuddStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
    }

    public static Integer getInkluderingstilskuddSats(LocalDate sluttDato) {
        return INKLUDERINGSTILSKUDD_SATS.hentGjeldendeSats(sluttDato == null ? LocalDate.MAX : sluttDato);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        sjekkTotalBeløp(nyAvtale);

        avtaleInnhold.getInkluderingstilskuddsutgift().clear();
        avtaleInnhold.getInkluderingstilskuddsutgift().addAll(nyAvtale.getInkluderingstilskuddsutgift());
        avtaleInnhold.getInkluderingstilskuddsutgift().forEach(i -> i.setAvtaleInnhold(avtaleInnhold));
        avtaleInnhold.setInkluderingstilskuddBegrunnelse(nyAvtale.getInkluderingstilskuddBegrunnelse());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());

        super.endre(nyAvtale);
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        var alleFelter = new HashMap<String, Object>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());

        alleFelter.put(AvtaleInnhold.Fields.inkluderingstilskuddsutgift, avtaleInnhold.getInkluderingstilskuddsutgift());
        alleFelter.put(AvtaleInnhold.Fields.inkluderingstilskuddBegrunnelse, avtaleInnhold.getInkluderingstilskuddBegrunnelse());
        alleFelter.put(AvtaleInnhold.Fields.harFamilietilknytning, avtaleInnhold.getHarFamilietilknytning());
        if (avtaleInnhold.getHarFamilietilknytning() != null && avtaleInnhold.getHarFamilietilknytning()) {
            alleFelter.put(AvtaleInnhold.Fields.familietilknytningForklaring, avtaleInnhold.getFamilietilknytningForklaring());
        }
        return alleFelter;
    }

    /**
     * Sjekker at summen av alle inkluderingstilskuddsbeløp ikke overstiger 143 900.
     * Beløpet er bestemt her: https://www.nav.no/arbeidsgiver/inkluderingstilskudd#hva
     */
    private void sjekkTotalBeløp(EndreAvtale avtale) {
        Integer sum = avtale.getInkluderingstilskuddsutgift().stream().map(Inkluderingstilskuddsutgift::getBeløp).reduce(0, Integer::sum);
        if (sum > getInkluderingstilskuddSats(avtale.getSluttDato())) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_SUM_FOR_HØY);
        } 
    }

}
