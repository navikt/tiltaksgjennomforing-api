package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erNoenTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class VTAOLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    private final LocalDate STANDARD_MIGRERINGSDATO = LocalDate.of(2025, 7, 1);

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(Avtale avtale) {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        if (erNoenTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato())) {
            return Collections.emptyList();
        }
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForVTAO(avtale);
        if (avtale.getArenaRyddeAvtale() != null || Avtaleopphav.ARENA.equals(avtale.getOpphav())) {
            LocalDate migreringsdato;
            if (avtale.getArenaRyddeAvtale() != null && avtale.getArenaRyddeAvtale().getMigreringsdato() != null) {
                migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato();
            } else {
                migreringsdato = STANDARD_MIGRERINGSDATO;
            }

            tilskuddsperioder.forEach(periode -> {
                if (periode.getSluttDato().minusDays(1).isBefore(migreringsdato)) {
                    periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                }
            });
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        return tilskuddsperioder;
    }

    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(
        Avtale avtale,
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForVTAOAvtale(
            startDato,
            sluttDato
        );
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    private static List<TilskuddPeriode> beregnTilskuddsperioderForVTAOAvtale(
        LocalDate datoFraOgMed,
        LocalDate datoTilOgMed
    ) {
        return BeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
            Integer beløp;
            var sats = VTAO_SATS.hentGjeldendeSats(datoPar.getStart());
            if (sats == null) {
                beløp = null;
            } else {
                beløp = BeregningStrategy.beløpForPeriode(
                    datoPar.getStart(),
                    datoPar.getSlutt(),
                    sats
                );
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
            sluttDato
        );
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
        return BeregningStrategy.beløpForPeriode(startDato, sluttDato, vtaoSats);
    }
}
