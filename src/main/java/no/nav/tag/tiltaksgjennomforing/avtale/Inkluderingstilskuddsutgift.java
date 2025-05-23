package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Data
@Entity
@FieldNameConstants
public class Inkluderingstilskuddsutgift {
    @Id
    @GeneratedValue
    private UUID id;
    private Integer beløp;
    @Enumerated(EnumType.STRING)
    private InkluderingstilskuddsutgiftType type;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold_id")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Inkluderingstilskuddsutgift() {}

    public Inkluderingstilskuddsutgift(Inkluderingstilskuddsutgift utgift) {
        id = null;
        beløp = utgift.beløp;
        type = utgift.type;
        avtaleInnhold = null;
    }
}
