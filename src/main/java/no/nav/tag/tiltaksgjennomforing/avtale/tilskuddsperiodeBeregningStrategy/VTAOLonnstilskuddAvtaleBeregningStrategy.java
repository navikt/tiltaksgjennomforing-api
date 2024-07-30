package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.*;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class VTAOLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    public void genererNyeTilskuddsperioder(Avtale avtale){
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        List<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        avtale.hentTilskuddsperioder().removeIf(t -> (t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) || (t.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA));
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();

        if(erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato())){
           tilskuddsperioder = beregnTilskuddsperioderForVTAO(avtale);
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        avtale.leggtilNyeTilskuddsperioder(tilskuddsperioder);
    }

    public void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato) {
        this.forleng(avtale, gammelSluttDato, nySluttDato);
    }

    /* Default */
    public List<TilskuddPeriode> beregnForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        List<TilskuddPeriode> tilskuddsperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForVTAOAvtale(
                avtale.getId(),
                avtale.getTiltakstype(),
                startDato,
                sluttDato);
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    /* VTAO */
    public static List<TilskuddPeriode> beregnTilskuddsperioderForVTAO(Avtale avtale){
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        LocalDate startDato = gjeldendeInnhold.getStartDato();
        LocalDate sluttDato = gjeldendeInnhold.getSluttDato();

        List<TilskuddPeriode> tilskuddsperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForVTAOAvtale(
                avtale.getId(),
                avtale.getTiltakstype(),
                startDato,
                sluttDato);
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(gjeldendeInnhold.getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(gjeldendeInnhold.getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    /* Reducer? */
    static void fikseLøpenumre(List<TilskuddPeriode> tilskuddperioder, int startPåLøpenummer) {
        for (int i = 0; i < tilskuddperioder.size(); i++) {
            tilskuddperioder.get(i).setLøpenummer(startPåLøpenummer + i);
        }
    }
}
