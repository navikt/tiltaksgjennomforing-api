package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TilskuddPeriode implements Comparable<TilskuddPeriode> {

    @Id
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY) // Unngå rekursiv query-loop (avtale.gjeldendetilskudd->tilskudd.avtale->...)
    @JoinColumn(name = "avtale_id")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

    private Integer beløp;
    @NonNull
    private LocalDate startDato;
    @NonNull
    private LocalDate sluttDato;

    @Convert(converter = NavIdentConverter.class)
    private NavIdent godkjentAvNavIdent;

    private LocalDateTime godkjentTidspunkt;

    private String enhet;
    private String enhetsnavn;

    private Integer lonnstilskuddProsent;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<Avslagsårsak> avslagsårsaker = EnumSet.noneOf(Avslagsårsak.class);

    private String avslagsforklaring;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent avslåttAvNavIdent;
    private LocalDateTime avslåttTidspunkt;
    private Integer løpenummer = 1;

    @Enumerated(EnumType.STRING)
    private TilskuddPeriodeStatus status = TilskuddPeriodeStatus.UBEHANDLET;

    @Enumerated(EnumType.STRING)
    private RefusjonStatus refusjonStatus = null;

    private boolean aktiv = true;

    public TilskuddPeriode(Integer beløp, @NonNull LocalDate start, @NonNull LocalDate slutt) {
        this.beløp = beløp;
        this.startDato = start;
        this.sluttDato = slutt;
    }

    public TilskuddPeriode(Integer beløp, @NonNull LocalDate start, @NonNull LocalDate slutt, Integer lonnstilskuddProsent) {
        this.beløp = beløp;
        this.startDato = start;
        this.sluttDato = slutt;
        this.lonnstilskuddProsent = lonnstilskuddProsent;
    }

    public TilskuddPeriode deaktiverOgLagNyUbehandlet() {
        this.aktiv = false;
        TilskuddPeriode kopi = new TilskuddPeriode();
        kopi.id = UUID.randomUUID();
        kopi.løpenummer = this.løpenummer;
        kopi.beløp = this.beløp;
        kopi.lonnstilskuddProsent = this.lonnstilskuddProsent;
        kopi.startDato = this.startDato;
        kopi.sluttDato = this.sluttDato;
        kopi.avtale = this.avtale;
        kopi.aktiv = true;
        kopi.status = TilskuddPeriodeStatus.UBEHANDLET;
        return kopi;
    }

    private void sjekkOmKanBehandles() {
        if (status != TilskuddPeriodeStatus.UBEHANDLET) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        if (Now.localDate().isBefore(kanBesluttesFom())) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_BEHANDLE_FOR_TIDLIG);
        }
    }

    private LocalDate tidligstI2026() {
        var startDatoMinus3Mnd = startDato.minusMonths(3);
        return startDatoMinus3Mnd.isBefore(TilskuddPeriode.JAN_2026) ? TilskuddPeriode.JAN_2026 : startDatoMinus3Mnd;
    }
    private boolean startDatoErI2026() {
        return startDato.isAfter(JAN_2026.minusDays(1));
    }

    private static final LocalDate JAN_2026 = LocalDate.of(2026, 1, 1);

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

        if (avtale.getTiltakstype() == Tiltakstype.VTAO) {
            return startDato.minusMonths(6);
        }
        return startDato.minusMonths(3);
    }

    void godkjenn(NavIdent beslutter, String enhet) {
        sjekkOmKanBehandles();

        setGodkjentTidspunkt(Now.localDateTime());
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

        setAvslåttTidspunkt(Now.localDateTime());
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
    public int compareTo(@NotNull TilskuddPeriode o) {
        return new CompareToBuilder()
                .append(this.getStartDato(), o.getStartDato())
                .append(this.isAktiv(), o.isAktiv())
                .append(this.getStatus(), o.getStatus())
                .append(this.getId(), o.getId())
                .toComparison();
    }

    public boolean erUtbetalt() {
        return refusjonStatus == RefusjonStatus.UTBETALT || refusjonStatus == RefusjonStatus.KORRIGERT;
    }

    public boolean erRefusjonGodkjent() {
        return refusjonStatus == RefusjonStatus.SENDT_KRAV || refusjonStatus == RefusjonStatus.GODKJENT_MINUSBELØP || refusjonStatus == RefusjonStatus.GODKJENT_NULLBELØP;
    }

}
