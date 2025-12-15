package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import lombok.NonNull;
import no.nav.tag.tiltaksgjennomforing.avtale.Avslagsårsak;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record TilskuddPeriodeDTO(
    UUID id,
    Integer beløp,
    @NonNull
    LocalDate startDato,
    @NonNull
    LocalDate sluttDato,
    NavIdent godkjentAvNavIdent,
    Instant godkjentTidspunkt,
    /**
     * "Enhet" i denne konteksten er oppfølgingsenheten til deltaker;
     * og betegnes også som "kostnadssted" i feks besluttervisning.
     * <p>
     * Beslutter kan velge å endre kostnadssted per tilskuddsperiode.
     * <p>
     * <b>Eksempel:</b> 1702 (Nav Inn-Trøndelag)
     */
    String enhet,
    String enhetsnavn,
    Integer lonnstilskuddProsent,
    Set<Avslagsårsak> avslagsårsaker,
    String avslagsforklaring,
    NavIdent avslåttAvNavIdent,
    Instant avslåttTidspunkt,
    Integer løpenummer,
    TilskuddPeriodeStatus status,
    RefusjonStatus refusjonStatus,
    boolean aktiv,
    LocalDate kanBesluttesFom,
    boolean kanBehandles
) implements Comparable<TilskuddPeriodeDTO> {
    private static final LocalDate JAN_2026 = LocalDate.of(2026, 1, 1);

    public TilskuddPeriodeDTO(TilskuddPeriode periode) {
        this(
            periode.getId(),
            periode.getBeløp(),
            periode.getStartDato(),
            periode.getSluttDato(),
            periode.getGodkjentAvNavIdent(),
            periode.getGodkjentTidspunkt(),
            periode.getEnhet(),
            periode.getEnhetsnavn(),
            periode.getLonnstilskuddProsent(),
            periode.getAvslagsårsaker(),
            periode.getAvslagsforklaring(),
            periode.getAvslåttAvNavIdent(),
            periode.getAvslåttTidspunkt(),
            periode.getLøpenummer(),
            periode.getStatus(),
            periode.getRefusjonStatus(),
            periode.isAktiv(),
            periode.kanBesluttesFom(),
            periode.kanBehandles()
        );
    }

    @Override
    public int compareTo(@NotNull TilskuddPeriodeDTO o) {
        return new CompareToBuilder()
            .append(this.løpenummer(), o.løpenummer())
            .append(this.startDato(), o.startDato())
            .append(this.sluttDato(), o.sluttDato())
            .append(this.aktiv(), o.aktiv())
            .append(this.status(), o.status())
            .append(this.refusjonStatus(), o.refusjonStatus())
            .append(this.beløp(), o.beløp())
            .append(this.godkjentTidspunkt(), o.godkjentTidspunkt())
            .append(this.enhet(), o.enhet())
            .append(this.enhetsnavn(), o.enhetsnavn())
            .append(this.lonnstilskuddProsent(), o.lonnstilskuddProsent())
            .append(this.avslåttTidspunkt(), o.avslåttTidspunkt())
            .append(this.avslagsforklaring(), o.avslagsforklaring())
            .append(this.avslagsårsaker().toString(), o.avslagsårsaker().toString())
            .append(
                Optional.ofNullable(this.godkjentAvNavIdent()).map(NavIdent::asString).orElse(null),
                Optional.ofNullable(o.godkjentAvNavIdent()).map(NavIdent::asString).orElse(null)
            )
            .append(
                Optional.ofNullable(this.avslåttAvNavIdent()).map(NavIdent::asString).orElse(null),
                Optional.ofNullable(o.avslåttAvNavIdent()).map(NavIdent::asString).orElse(null)
            )
            .toComparison();
    }
}
