package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TilskuddPeriode implements Comparable<TilskuddPeriode> {

    @Id
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "avtale_id")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

    @NonNull
    private Integer beløp;
    @NonNull
    private LocalDate startDato;
    @NonNull
    private LocalDate sluttDato;

    @Convert(converter = NavIdentConverter.class)
    private NavIdent godkjentAvNavIdent;

    private LocalDateTime godkjentTidspunkt;

    @NonNull
    private Integer lonnstilskuddProsent;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Avslagsårsak> avslagsårsaker = EnumSet.noneOf(Avslagsårsak.class);

    private String avslagsforklaring;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent avslåttAvNavIdent;
    private LocalDateTime avslåttTidspunkt;

    @Enumerated(EnumType.STRING)
    private TilskuddPeriodeStatus status = TilskuddPeriodeStatus.UBEHANDLET;

    public TilskuddPeriode(TilskuddPeriode periode) {
        id = UUID.randomUUID();
        beløp = periode.beløp;
        startDato = periode.startDato;
        sluttDato = periode.sluttDato;
        status = periode.status;
        lonnstilskuddProsent = periode.lonnstilskuddProsent;
    }

    private void sjekkOmKanBehandles() {
        if (status != TilskuddPeriodeStatus.UBEHANDLET) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        if (LocalDate.now().isBefore(startDato.minusWeeks(2))) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_BEHANDLE_FOR_TIDLIG);
        }
    }

    void godkjenn(NavIdent beslutter) {
        sjekkOmKanBehandles();

        setGodkjentTidspunkt(LocalDateTime.now());
        setGodkjentAvNavIdent(beslutter);
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

        setAvslåttTidspunkt(LocalDateTime.now());
        setAvslåttAvNavIdent(beslutter);
        this.avslagsårsaker.addAll(avslagsårsaker);
        setAvslagsforklaring(avslagsforklaring);
        setStatus(TilskuddPeriodeStatus.AVSLÅTT);
    }

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
                .append(this.getStatus(), o.getStatus())
                .append(this.getId(), o.getId())
                .toComparison();
    }
}
