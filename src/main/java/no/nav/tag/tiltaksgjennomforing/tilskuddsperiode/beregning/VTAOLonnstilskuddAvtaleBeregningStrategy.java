package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class VTAOLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    public void genererNyeTilskuddsperioder(Avtale avtale){
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        List<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        avtale.getTilskuddPeriode().removeIf(t -> (t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) || (t.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA));
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();

        if(erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato())){
           tilskuddsperioder = beregnTilskuddsperioderForVTAO(avtale);
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        avtale.leggtilNyeTilskuddsperioder(tilskuddsperioder);
    }
    public List<TilskuddPeriode> opprettTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForVTAOAvtale(
                startDato,
                sluttDato);
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    private static List<TilskuddPeriode>  beregnTilskuddsperioderForVTAOAvtale(LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        List<TilskuddPeriode> tilskuddperioder = LonnstilskuddAvtaleBeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
            Integer beløp = LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), 6808);
            return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), 0);
        }).toList();
        return tilskuddperioder;
    }

    public List<TilskuddPeriode> beregnTilskuddsperioderForVTAO(Avtale avtale){
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        LocalDate startDato = gjeldendeInnhold.getStartDato();
        LocalDate sluttDato = gjeldendeInnhold.getSluttDato();

        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForVTAOAvtale(
                startDato,
                sluttDato);
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(gjeldendeInnhold.getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(gjeldendeInnhold.getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }
}
