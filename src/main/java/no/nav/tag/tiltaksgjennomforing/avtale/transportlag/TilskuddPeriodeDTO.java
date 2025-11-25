package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.avtale.Avslagsårsak;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record TilskuddPeriodeDTO(

    UUID id,

    @JsonIgnore
    @ToString.Exclude
    Avtale avtale,

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

    boolean aktiv
) implements Comparable<TilskuddPeriodeDTO> {
    private static final LocalDate JAN_2026 = LocalDate.of(2026, 1, 1);

    public TilskuddPeriodeDTO(TilskuddPeriode periode) {
        this(
            periode.getId(),
            periode.getAvtale(),
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
            periode.isAktiv()
        );
    }

    private void sjekkOmKanBehandles() {
        if (status != TilskuddPeriodeStatus.UBEHANDLET) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        if (Now.localDate().isBefore(kanBesluttesFom())
            || beløp() == null) {
            // beløp kan være null når vi fortsatt ikke har sats for en VTAO-avtale
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_BEHANDLE_FOR_TIDLIG);
        }
        if (avtale.getKreverOppfolgingFrist() != null && startDato.isAfter(avtale.getKreverOppfolgingFrist())) {
            throw new FeilkodeException(Feilkode.KREVER_OPPFØLGING_AV_VTAO);
        }
    }

    private LocalDate tidligstI2026() {
        var startDatoMinus3Mnd = startDato.minusMonths(3);
        return startDatoMinus3Mnd.isBefore(TilskuddPeriodeDTO.JAN_2026) ? TilskuddPeriodeDTO.JAN_2026 : startDatoMinus3Mnd;
    }

    private boolean startDatoErI2026() {
        return startDato.isAfter(JAN_2026.minusDays(1));
    }

    @JsonProperty
    private LocalDate kanBesluttesFom() {
        // Ikke tillat godkjenning av tilskuddsperioder etter 2026 før budsjettet er vedtatt
        // TODO: Må oppdateres før årsskifte 2025/2026
        if (startDatoErI2026()) {
            return tidligstI2026();
        }
        if (løpenummer == 1) {
            return LocalDate.MIN;
        }
        return startDato.minusMonths(3);
    }

    @JsonProperty
    public boolean kanBehandles() {
        try {
            sjekkOmKanBehandles();
            return true;
        } catch (FeilkodeException e) {
            return false;
        }
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
            .append(this.avtale(), o.avtale())
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

    public boolean erUtbetalt() {
        return refusjonStatus == RefusjonStatus.UTBETALT || refusjonStatus == RefusjonStatus.KORRIGERT;
    }

    public boolean erRefusjonGodkjent() {
        return refusjonStatus == RefusjonStatus.SENDT_KRAV || refusjonStatus == RefusjonStatus.GODKJENT_MINUSBELØP || refusjonStatus == RefusjonStatus.GODKJENT_NULLBELØP;
    }

}
