package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
public class Inkluderingstilskuddsutgift {
    @Id
    @GeneratedValue
    private UUID id;
    private Integer beløp;
    @Enumerated(EnumType.STRING)
    private InkluderingstilskuddsutgiftType type;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Inkluderingstilskuddsutgift() {

    }

    public Inkluderingstilskuddsutgift(Inkluderingstilskuddsutgift fra) {
        id = UUID.randomUUID();
        beløp = fra.beløp;
        type = fra.type;
    }
}
