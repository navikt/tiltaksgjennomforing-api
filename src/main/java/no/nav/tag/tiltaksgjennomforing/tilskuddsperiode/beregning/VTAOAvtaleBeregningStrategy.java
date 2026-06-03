package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class VTAOAvtaleBeregningStrategy extends BeregningStrategy {
    private final LocalDate STANDARD_MIGRERINGSDATO = ArenaTiltakskode.VTAO.getMigreringsdatoForTilskudd();

    public VTAOAvtaleBeregningStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void endreBeregning(EndreTilskuddsberegning endreTilskuddsberegning) {}

    @Override
    public void reberegnTotal() {}

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp() {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder() {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder() {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder()) {
            return Collections.emptyList();
        }
        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForAvtale(
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
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        return Periode.av(startDato, sluttDato).splitPerMnd().stream().map(datoPar -> {
            var sats = VTAO_SATS.hentGjeldendeSats(datoPar.getStart());
            Integer beløp = sats != null ? BeregningStrategy.beløpForPeriode(datoPar, sats) : null;
            TilskuddPeriode tilskuddPeriode = new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt());
            tilskuddPeriode.setAvtale(avtale);
            tilskuddPeriode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
            tilskuddPeriode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
            return tilskuddPeriode;
        }).toList();
    }

    @Override
    public Integer getBeløpForPeriode(AvtaleInnhold avtaleInnhold, Periode periode) {
        var vtaoSats = VTAO_SATS.hentGjeldendeSats(periode.getStart());
        if (vtaoSats == null) {
            return null;
        }
        return BeregningStrategy.beløpForPeriode(periode, vtaoSats);
    }
}
