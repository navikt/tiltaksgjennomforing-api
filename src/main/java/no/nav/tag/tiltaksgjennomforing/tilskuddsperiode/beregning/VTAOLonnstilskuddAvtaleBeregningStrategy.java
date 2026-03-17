package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class VTAOLonnstilskuddAvtaleBeregningStrategy implements BeregningStrategy {
    private final LocalDate STANDARD_MIGRERINGSDATO = ArenaTiltakskode.VTAO.getMigreringsdatoForTilskudd();

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {}

    @Override
    public void reberegnTotal(Avtale avtale) {}

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(Avtale avtale) {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(Avtale avtale) {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(avtale)) {
            return Collections.emptyList();
        }
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForAvtale(
            avtale,
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato()
        );
        if (avtale.getArenaRyddeAvtale() != null || Avtaleopphav.ARENA.equals(avtale.getOpphav())) {
            LocalDate migreringsdato;
            if (avtale.getArenaRyddeAvtale() != null && avtale.getArenaRyddeAvtale().getMigreringsdato() != null) {
                migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato();
            } else {
                migreringsdato = STANDARD_MIGRERINGSDATO;
            }

            BeregningStrategy.settBehandletIArena(migreringsdato, tilskuddsperioder);
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        return tilskuddsperioder;
    }

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(
        Avtale avtale,
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        return BeregningStrategy.lagPeriode(startDato, sluttDato).stream().map(datoPar -> {
            var sats = VTAO_SATS.hentGjeldendeSats(datoPar.getStart());
            Integer beløp = sats != null ? BeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sats) : null;
            TilskuddPeriode tilskuddPeriode = new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt());
            tilskuddPeriode.setAvtale(avtale);
            tilskuddPeriode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
            tilskuddPeriode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
            return tilskuddPeriode;
        }).toList();
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
