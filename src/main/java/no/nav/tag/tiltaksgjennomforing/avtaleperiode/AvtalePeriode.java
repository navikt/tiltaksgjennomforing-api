package no.nav.tag.tiltaksgjennomforing.avtaleperiode;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.RegnUtTilskuddsperioderForAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

@Data
public class AvtalePeriode {
    private final UUID avtaleId;
    private final NavigableSet<TilskuddPeriode> tilskuddperioder = new TreeSet<>();
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer beløpPerMåned;
    private Integer lønnstilskuddProsent;

    public AvtalePeriode(UUID avtaleId, LocalDate startDato, LocalDate avtaleSluttDato, Integer beløpPerMåned, Integer lønnstilskuddProsent) {
        this.avtaleId = avtaleId;
        this.startDato = startDato;
        this.sluttDato = avtaleSluttDato;
        this.beløpPerMåned = beløpPerMåned;
        this.lønnstilskuddProsent = lønnstilskuddProsent;
        nyeTilskuddsperioder();
    }

    private void nyeTilskuddsperioder() {
        List<TilskuddPeriode> tilskuddperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(beløpPerMåned, startDato, sluttDato, lønnstilskuddProsent, null, null);
        this.tilskuddperioder.addAll(tilskuddperioder);
    }

    public void forlengAvtale(LocalDate avtaleSluttDato) {
        this.sluttDato = avtaleSluttDato;

        List<TilskuddPeriode> tilskuddsperioderSomSkalLeggesTil;
        TilskuddPeriode sisteTilskuddsperiode = tilskuddperioder.last();
        if (sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            // Kan utvide siste tilskuddsperiode hvis den er ubehandlet
            tilskuddperioder.remove(sisteTilskuddsperiode);
            tilskuddsperioderSomSkalLeggesTil = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(beløpPerMåned, sisteTilskuddsperiode.getStartDato(), avtaleSluttDato, lønnstilskuddProsent, null, null);
        } else {
            // Regner ut nye perioder fra gammel avtaleslutt til ny avtaleslutt
            tilskuddsperioderSomSkalLeggesTil = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(beløpPerMåned, this.sluttDato, avtaleSluttDato, lønnstilskuddProsent, null, null);
        }
        tilskuddsperioderSomSkalLeggesTil.addAll(tilskuddsperioderSomSkalLeggesTil);
    }

    public void forkortAvtale(LocalDate avtaleSluttDato) {
        this.sluttDato = avtaleSluttDato;

        for (TilskuddPeriode tilskuddPeriode : tilskuddperioder.descendingSet()) {
            TilskuddPeriodeStatus status = tilskuddPeriode.getStatus();
            if (tilskuddPeriode.getStartDato().isAfter(avtaleSluttDato)) {
                if (status == TilskuddPeriodeStatus.UBEHANDLET) {
                    tilskuddperioder.remove(tilskuddPeriode);
                } else {
                    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.ANNULLERT);
                }
            } else if (tilskuddPeriode.getSluttDato().isAfter(avtaleSluttDato)) {
                if (status == TilskuddPeriodeStatus.UBEHANDLET) {
                    tilskuddPeriode.setSluttDato(avtaleSluttDato);
                    tilskuddPeriode.setBeløp(RegnUtTilskuddsperioderForAvtale.beløpForPeriode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato(), beløpPerMåned));
                } else if (status == TilskuddPeriodeStatus.GODKJENT) {
                    TilskuddPeriode ny = tilskuddPeriode.annullerOgLagNyForkortet(avtaleSluttDato);
                    tilskuddperioder.add(ny);
                }
            }
        }
    }

    public void endreBeløp(Integer beløpPerMåned) {
        this.beløpPerMåned = beløpPerMåned;

        for (TilskuddPeriode tilskuddPeriode : tilskuddperioder) {
            if (tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
                tilskuddPeriode.setBeløp(RegnUtTilskuddsperioderForAvtale.beløpForPeriode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato(), beløpPerMåned));
            } else if (tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.GODKJENT) {
                TilskuddPeriode ny = tilskuddPeriode.annullerOgLagNy();
                tilskuddperioder.add(ny);
            }
        }
    }
}
