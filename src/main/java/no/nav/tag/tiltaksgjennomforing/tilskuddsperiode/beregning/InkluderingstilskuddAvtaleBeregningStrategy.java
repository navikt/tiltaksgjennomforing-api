package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.apache.commons.lang3.NotImplementedException;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
    public boolean harNyeTilskuddsperioder(Set<TilskuddPeriode> eksisterende, List<TilskuddPeriode> nye) {
        // Utgifter kobles til de nye periodeinstansene, så alle må tilskuddsperioder må erstattes.
        return true;
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp() {
        return nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder();
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder() {
        return Utils.erIkkeTomme(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift());
    }

    @Override
    public void forleng(LocalDate gammelSluttDato, LocalDate nySluttDato) {
        // Ikke gjør noe ved forlengig
    }

    @Override
    public void forkort(LocalDate nySluttDato) {
        Set<TilskuddPeriode> tilskuddPeriode = avtale.getTilskuddPeriode();
        for (TilskuddPeriode tp : Set.copyOf(tilskuddPeriode)) {
            TilskuddPeriodeStatus status = tp.getStatus();
            if (!status.erUbehandletEllerGodkjent()) {
                continue;
            }

            boolean skalFjernes = tp.getStartDato().isAfter(nySluttDato);
            if (skalFjernes) {
                tilskuddPeriode.remove(tp);
                if (status.erGodkjent()) {
                    tp.annuller();
                    tilskuddPeriode.add(tp);
                }
                continue;
            }

            boolean skalForkortes = tp.getSluttDato().isAfter(nySluttDato);
            if (skalForkortes) {
                tilskuddPeriode.remove(tp);
                tp.forkort(nySluttDato);
                tilskuddPeriode.add(tp);
            }
        }
    }

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder() {
        if (avtale.erAvtaleInngått()) {
            return avtale.getTilskuddPeriode().stream().toList();
        }
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder()) {
            return Collections.emptyList();
        }
        List<Inkluderingstilskuddsutgift> utgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        List<TilskuddPeriode> eksisterende = utgifter.stream()
            .map(Inkluderingstilskuddsutgift::getTilskuddperiode)
            .filter(Objects::nonNull)
            .toList();
        List<Inkluderingstilskuddsutgift> utgifterUtenPeriode = utgifter.stream()
            .filter(utgift -> utgift.getTilskuddperiode() == null)
            .toList();

        if (utgifterUtenPeriode.isEmpty()) {
            return eksisterende;
        }

        List<TilskuddPeriode> nye = lagTilskuddsperioder(utgifterUtenPeriode, sisteLøpenummer(eksisterende) + 1);
        return Stream.concat(eksisterende.stream(), nye.stream()).toList();
    }

    @Override
    public List<TilskuddPeriode> genererTilskuddsperioder(GenererTilskuddsperioder periode) {
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder()) {
            return Collections.emptyList();
        }

        List<Inkluderingstilskuddsutgift> utgifter = GenererTilskuddsperioder.konverterTilUtgifter(periode).utgifter();
        return lagTilskuddsperioder(utgifter, sisteLøpenummer(avtale.getTilskuddPeriode()) + 1);
    }

    private static int sisteLøpenummer(Collection<TilskuddPeriode> perioder) {
        return perioder.stream()
            .map(TilskuddPeriode::getLøpenummer)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
    }

    List<TilskuddPeriode> lagTilskuddsperioder(List<Inkluderingstilskuddsutgift> utgifter, int lopenummer) {
        if (utgifter.isEmpty()) {
            return Collections.emptyList();
        }

        List<TilskuddPeriode> perioder = utgifter.stream().map(utgift -> {
            TilskuddPeriode tilskuddsperiode = lagTilskuddsperiode(utgift);
            utgift.setTilskuddperiode(tilskuddsperiode);
            return tilskuddsperiode;
        }).toList();

        fikseLøpenumre(perioder, lopenummer);

        return perioder;
    }

    TilskuddPeriode lagTilskuddsperiode(Inkluderingstilskuddsutgift utgift) {
        LocalDate start = Optional.ofNullable(utgift.getGenererTilskuddsperiodeData())
            .map(Inkluderingstilskuddsutgift.GenererTilskuddsperiodeData::startdato)
            .orElse(null);
        LocalDate slutt = Optional.ofNullable(utgift.getGenererTilskuddsperiodeData())
            .map(Inkluderingstilskuddsutgift.GenererTilskuddsperiodeData::sluttdato)
            .orElse(null);

        LocalDate avtaleStart = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate avtaleSlutt = avtale.getGjeldendeInnhold().getSluttDato();
        if (avtaleStart == null || avtaleSlutt == null) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_AVTALE_MANGLER_DATO);
        }
        if (start == null || slutt == null) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_UTGIFT_MANGLER_DATO);
        }
        if (slutt.isBefore(start)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_AVTALE_START_ETTER_SLUTT);
        }
        if (start.isBefore(avtaleStart)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_FOR_TIDLIG_STARTDATO);
        }
        if (slutt.isAfter(avtaleSlutt)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_FOR_SEN_SLUTTDATO);
        }
        if (!slutt.isBefore(start.plusMonths(3))) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_FOR_LANG);
        }
        LocalDate sistedagIAaret = start.with(TemporalAdjusters.lastDayOfYear());
        if (slutt.isAfter(sistedagIAaret)) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_PAA_TVERS_AV_AAR);
        }

        TilskuddPeriode tilskuddsperiode = new TilskuddPeriode(utgift.getBeløp(), start, slutt);
        tilskuddsperiode.setAvtale(avtale);
        tilskuddsperiode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
        tilskuddsperiode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
        return tilskuddsperiode;
    }
}
