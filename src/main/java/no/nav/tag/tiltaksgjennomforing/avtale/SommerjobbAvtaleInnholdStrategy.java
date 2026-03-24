package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.SommerjobbLonnstilskuddAvtaleBeregningStrategy;

import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.SommerjobbLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_MAKS;
import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.SommerjobbLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_MIN;


public class SommerjobbAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<SommerjobbLonnstilskuddAvtaleBeregningStrategy> {
    public SommerjobbAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new SommerjobbLonnstilskuddAvtaleBeregningStrategy());
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddProsent, avtaleInnhold.getLonnstilskuddProsent());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();
        endreAvtale.setStillingstype(Stillingstype.MIDLERTIDIG);

        if (!erTilskuddsprosentGyldig(lonnstilskuddProsent)) {
            throw new FeilLonnstilskuddsprosentException();
        }

        avtaleInnhold.setLonnstilskuddProsent(lonnstilskuddProsent);
        super.endre(endreAvtale);
    }

    private boolean erTilskuddsprosentGyldig(Integer prosent) {
        if (prosent == null) {
            return true;
        }
        return prosent == TILSKUDDSPROSENT_MAKS || prosent == TILSKUDDSPROSENT_MIN;
    }

}
