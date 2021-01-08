package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class TilskuddPeriode {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "avtale_innhold")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    @NonNull
    private Integer beløp;
    @NonNull
    private LocalDate startDato;
    @NonNull
    private LocalDate sluttDato;

    @Convert(converter = NavIdentConverter.class)
    private NavIdent godkjentAvNavIdent;

    private LocalDateTime godkjentTidspunkt;

    public TilskuddPeriode(TilskuddPeriode periode) {
        id = UUID.randomUUID();
        beløp = periode.beløp;
        startDato = periode.startDato;
        sluttDato = periode.sluttDato;
    }

    public boolean erGodkjent() {
        return godkjentTidspunkt != null && godkjentAvNavIdent != null;
    }

    public void godkjenn(NavIdent beslutter) {
        if (erGodkjent()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_GODKJENT);
        }
        if (!getAvtaleInnhold().getAvtale().erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_GODKJENNES_VED_INNGAATT_AVTALE);
        }
        if (startDato.isBefore(LocalDate.now().minusWeeks(2))) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_GODKJENT_FOR_TIDLIG);
        }
        setGodkjentTidspunkt(LocalDateTime.now());
        setGodkjentAvNavIdent(beslutter);
    }
}
