package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.avtale.Avslagsårsak;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class TilskuddPeriodeDTO implements Comparable<TilskuddPeriodeDTO> {
    private static final LocalDate JAN_2026 = LocalDate.of(2026, 1, 1);

    private UUID id = UUID.randomUUID();

    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

    private Integer beløp;
    @NonNull
    private LocalDate startDato;
    @NonNull
    private LocalDate sluttDato;

    private NavIdent godkjentAvNavIdent;

    private Instant godkjentTidspunkt;

    /**
     * "Enhet" i denne konteksten er oppfølgingsenheten til deltaker;
     * og betegnes også som "kostnadssted" i feks besluttervisning.
     * <p>
     * Beslutter kan velge å endre kostnadssted per tilskuddsperiode.
     * <p>
     * <b>Eksempel:</b> 1702 (Nav Inn-Trøndelag)
     */
    private String enhet;
    private String enhetsnavn;

    private Integer lonnstilskuddProsent;

    private Set<Avslagsårsak> avslagsårsaker = EnumSet.noneOf(Avslagsårsak.class);

    private String avslagsforklaring;
    private NavIdent avslåttAvNavIdent;
    private Instant avslåttTidspunkt;
    private Integer løpenummer = 1;

    private TilskuddPeriodeStatus status = TilskuddPeriodeStatus.UBEHANDLET;

    private RefusjonStatus refusjonStatus = null;

    private boolean aktiv = true;

    private void sjekkOmKanBehandles() {
        if (status != TilskuddPeriodeStatus.UBEHANDLET) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        if (Now.localDate().isBefore(kanBesluttesFom())
            || getBeløp() == null) {
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

    void godkjenn(NavIdent beslutter, String enhet) {
        sjekkOmKanBehandles();

        setGodkjentTidspunkt(Now.instant());
        setGodkjentAvNavIdent(beslutter);
        setEnhet(enhet);
        setStatus(TilskuddPeriodeStatus.GODKJENT);
    }

    void avslå(NavIdent beslutter, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkOmKanBehandles();
        if (avslagsforklaring.isBlank()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_AVSLAGSFORKLARING_PAAKREVD);
        }
        if (avslagsårsaker.isEmpty()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_INGEN_AVSLAGSAARSAKER);
        }

        setAvslåttTidspunkt(Now.instant());
        setAvslåttAvNavIdent(beslutter);
        this.avslagsårsaker.addAll(avslagsårsaker);
        setAvslagsforklaring(avslagsforklaring);
        setStatus(TilskuddPeriodeStatus.AVSLÅTT);
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
            .append(this.getLøpenummer(), o.getLøpenummer())
            .append(this.getStartDato(), o.getStartDato())
            .append(this.getSluttDato(), o.getSluttDato())
            .append(this.isAktiv(), o.isAktiv())
            .append(this.getStatus(), o.getStatus())
            .append(this.getRefusjonStatus(), o.getRefusjonStatus())
            .append(this.getBeløp(), o.getBeløp())
            .append(this.getAvtale(), o.getAvtale())
            .append(this.getGodkjentTidspunkt(), o.getGodkjentTidspunkt())
            .append(this.getEnhet(), o.getEnhet())
            .append(this.getEnhetsnavn(), o.getEnhetsnavn())
            .append(this.getLonnstilskuddProsent(), o.getLonnstilskuddProsent())
            .append(this.getAvslåttTidspunkt(), o.getAvslåttTidspunkt())
            .append(this.getAvslagsforklaring(), o.getAvslagsforklaring())
            .append(this.getAvslagsårsaker().toString(), o.getAvslagsårsaker().toString())
            .append(
                Optional.ofNullable(this.getGodkjentAvNavIdent()).map(NavIdent::asString).orElse(null),
                Optional.ofNullable(o.getGodkjentAvNavIdent()).map(NavIdent::asString).orElse(null)
            )
            .append(
                Optional.ofNullable(this.getAvslåttAvNavIdent()).map(NavIdent::asString).orElse(null),
                Optional.ofNullable(o.getAvslåttAvNavIdent()).map(NavIdent::asString).orElse(null)
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
