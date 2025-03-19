package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class VTAOLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    private  final LocalDate STANDARD_MIGRERINGSDATO = LocalDate.of(2025, 7, 1);

    public void genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        List<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        avtale.getTilskuddPeriode().removeIf(t -> (t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) || (t.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA));
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();

        if (erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato())) {
            tilskuddsperioder = beregnTilskuddsperioderForVTAO(avtale);
            if (avtale.getArenaRyddeAvtale() != null || Avtaleopphav.ARENA.equals(avtale.getOpphav())) {
                LocalDate migreringsdato;
                if (avtale.getArenaRyddeAvtale() != null && avtale.getArenaRyddeAvtale().getMigreringsdato() != null) {
                    migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato();
                } else {
                    migreringsdato = STANDARD_MIGRERINGSDATO;
                }

                tilskuddsperioder.forEach(periode -> {
                    // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                    // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                    if (periode.getSluttDato().minusDays(1).isBefore(migreringsdato)) {
                        periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                    }
                });
            }
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        avtale.leggtilNyeTilskuddsperioder(tilskuddsperioder);
    }

    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForVTAOAvtale(
                startDato,
                sluttDato);
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    private static List<TilskuddPeriode> beregnTilskuddsperioderForVTAOAvtale(LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        return LonnstilskuddAvtaleBeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
            Integer beløp;
            var sats = VTAO_SATS.hentGjeldendeSats(datoPar.getStart());
            if (sats == null) {
                beløp = null;
            } else {
                beløp = LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sats);
            }
            return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt());
        }).toList();
    }

    public List<TilskuddPeriode> beregnTilskuddsperioderForVTAO(Avtale avtale) {
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

    @Override
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        var vtaoSats = VTAO_SATS.hentGjeldendeSats(startDato);
        if (vtaoSats == null) {
            return null;
        }
        return LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(startDato, sluttDato, vtaoSats);
    }
}
