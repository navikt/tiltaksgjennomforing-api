package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.InkluderingstilskuddsutgiftDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@FieldNameConstants
public class Inkluderingstilskuddsutgift {
    @Id
    @GeneratedValue
    private UUID id;
    private String navn;
    private Integer beløp;
    private String beskrivelse;
    @Enumerated(EnumType.STRING)
    private InkluderingstilskuddsutgiftType type;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold_id")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;
    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private TilskuddPeriode tilskuddperiode;
    @JsonIgnore
    @ToString.Exclude
    @Transient
    private GenererTilskuddsperiodeData genererTilskuddsperiodeData;

    public record GenererTilskuddsperiodeData(
        LocalDate startdato,
        LocalDate sluttdato
    ) {}

    public Inkluderingstilskuddsutgift() {}

    public Inkluderingstilskuddsutgift(Inkluderingstilskuddsutgift utgift) {
        id = null;
        navn = utgift.navn;
        beløp = utgift.beløp;
        beskrivelse = utgift.beskrivelse;
        type = utgift.type;
        tilskuddperiode = utgift.tilskuddperiode;
    }

    public Inkluderingstilskuddsutgift(InkluderingstilskuddsutgiftDTO dto) {
        id = null;
        navn = dto.navn();
        beløp = dto.beløp();
        beskrivelse = dto.beskrivelse();
        type = dto.type();
        genererTilskuddsperiodeData = new GenererTilskuddsperiodeData(
            dto.startdato(),
            dto.sluttdato()
        );
    }

    public static List<Inkluderingstilskuddsutgift> map(List<InkluderingstilskuddsutgiftDTO> dto) {
        return dto.stream().map(Inkluderingstilskuddsutgift::new).toList();
    }
}
