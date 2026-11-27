package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreAvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.InkluderingstilskuddsutgiftDTO;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode.FANT_UTGIFTER_SOM_IKKE_TIDLIGERE_ER_LAGRET;
import static no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle.NY_INKLUDERINGSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.satser.Sats.INKLUDERINGSTILSKUDD_SATS;

public class InkluderingstilskuddAvtaleInnholdStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
    }

    public static Integer getInkluderingstilskuddSats(LocalDate sluttDato) {
        return INKLUDERINGSTILSKUDD_SATS.hentGjeldendeSats(sluttDato == null ? LocalDate.MAX : sluttDato);
    }

    @Override
    public void endre(EndreAvtaleDTO nyAvtale) {
        List<Inkluderingstilskuddsutgift> utgifter = nyAvtale.getInkluderingstilskuddsutgift().stream().map(Inkluderingstilskuddsutgift::new).toList();
        sjekkTotalBeløp(nyAvtale.getSluttDato(), utgifter);

        Set<UUID> gjeldendeUtgifter = avtaleInnhold.getInkluderingstilskuddsutgift().stream()
            .map(Inkluderingstilskuddsutgift::getId)
            .collect(Collectors.toSet());
        Set<UUID> nyeUtgifter = nyAvtale.getInkluderingstilskuddsutgift().stream()
            .map(InkluderingstilskuddsutgiftDTO::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!gjeldendeUtgifter.containsAll(nyeUtgifter)) {
            throw new FeilkodeException(FANT_UTGIFTER_SOM_IKKE_TIDLIGERE_ER_LAGRET);
        }

        avtaleInnhold.getInkluderingstilskuddsutgift().forEach(utgift -> utgift.setTilskuddperiode(null));
        avtaleInnhold.getInkluderingstilskuddsutgift().clear();
        avtaleInnhold.getInkluderingstilskuddsutgift().addAll(utgifter);
        avtaleInnhold.getInkluderingstilskuddsutgift().forEach(utgift -> utgift.setAvtaleInnhold(avtaleInnhold));
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
    private static void sjekkTotalBeløp(LocalDate sluttdato, List<Inkluderingstilskuddsutgift> utgifter) {
        Integer sum = utgifter.stream().map(Inkluderingstilskuddsutgift::getBeløp).reduce(0, Integer::sum);
        if (sum > getInkluderingstilskuddSats(sluttdato)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_SUM_FOR_HØY);
        }
    }

    @Override
    public void sjekkGodkjenning() {
        if (FeatureToggleHolder.get().isEnabled(NY_INKLUDERINGSTILSKUDD) && avtaleInnhold.getInkluderingstilskuddsutgift().stream().anyMatch(i -> i.getTilskuddperiode() == null)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_UTGIFT_MANGLER_TILSKUDDSPERIODE);
        }
    }
}
