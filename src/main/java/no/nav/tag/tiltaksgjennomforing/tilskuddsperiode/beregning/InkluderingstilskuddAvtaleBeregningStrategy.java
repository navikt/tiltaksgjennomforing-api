package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.apache.commons.lang3.NotImplementedException;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_ETTER_SLUTTDATO;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class InkluderingstilskuddAvtaleBeregningStrategy extends BeregningStrategy {
    public InkluderingstilskuddAvtaleBeregningStrategy(Avtale avtale) {
        super(avtale);
    }

    @Override
    public void endreBeregning(EndreTilskuddsberegning endreTilskuddsberegning) {
        throw new NotImplementedException("Endring av tilskuddsberegning er ikke implementert");
    }

    @Override
    public void reberegnTotal() {}

    @Override
    public boolean harNyeTilskuddsperioder(Set<TilskuddPeriode> a, List<TilskuddPeriode> b) {
        // Alltid lag nye perioder siden utgifter er koblet til tilskudd og blir overskrevet ved generering av nye perioder.
        return true;
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp() {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getInkluderingstilskuddsutgift(),
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder() {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getInkluderingstilskuddsutgift(),
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
    }

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder() {
        if (avtale.erAvtaleInngått()) {
            return Collections.emptyList();
        }
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder()) {
            return Collections.emptyList();
        }

        List<Inkluderingstilskuddsutgift> utgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        return lagTilskuddsperioder(utgifter, 1);
    }

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(LocalDate start, LocalDate slutt) {
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder()) {
            return Collections.emptyList();
        }

        List<Inkluderingstilskuddsutgift> nyeUtgifter = avtale.getGjeldendeInnhold()
            .getInkluderingstilskuddsutgift()
            .stream()
            .filter(utgift -> utgift.getTilskuddperiode() == null)
            .toList();

        int sisteLopenummer = avtale.getTilskuddPeriode()
            .stream()
            .map(TilskuddPeriode::getLøpenummer)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);

        return lagTilskuddsperioder(nyeUtgifter, sisteLopenummer + 1);
    }

    List<TilskuddPeriode> lagTilskuddsperioder(List<Inkluderingstilskuddsutgift> utgifter, int lopenummer) {
        if (utgifter.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate avtaleStartdato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate avtaleSluttdato = avtale.getGjeldendeInnhold().getSluttDato();
        List<TilskuddPeriode> perioder = utgifter.stream().map(utgift -> {
            if (utgift.getTilskuddperiode() != null) {
                return utgift.getTilskuddperiode();
            }
            TilskuddPeriode tilskuddsperiode = lagTilskuddsperiode(utgift, avtaleStartdato, avtaleSluttdato);
            utgift.setTilskuddperiode(tilskuddsperiode);
            return tilskuddsperiode;
        }).toList();

        fikseLøpenumre(perioder, lopenummer);

        return perioder;
    }

    TilskuddPeriode lagTilskuddsperiode(Inkluderingstilskuddsutgift utgift, LocalDate avtaleStartdato, LocalDate avtaleSluttdato) {
        LocalDate sistedagIAaret = avtaleStartdato.with(TemporalAdjusters.lastDayOfYear());

        LocalDate startdato = DatoUtils.maksDato(avtaleStartdato, Now.localDate());
        LocalDate sluttdato = DatoUtils.minDato(avtaleSluttdato, startdato.plusMonths(3), sistedagIAaret);

        if (startdato.getYear() != sistedagIAaret.getYear()) {
            throw new FeilkodeException(KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_ETTER_SLUTTDATO);
        }

        TilskuddPeriode tilskuddsperiode = new TilskuddPeriode(utgift.getBeløp(), startdato, sluttdato);
        tilskuddsperiode.setAvtale(avtale);
        tilskuddsperiode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
        tilskuddsperiode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
        return tilskuddsperiode;
    }
}
