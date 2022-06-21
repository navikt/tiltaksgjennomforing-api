package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
public class Inkluderingstilskudd {
    @Id
    @GeneratedValue
    private UUID id;
    private Integer beløp;
    @Enumerated(EnumType.STRING)
    private Inkluderingstilskuddtyper type;
    private String forklaring;
    @ManyToOne
    @JoinColumn(name = "avtale_innhold")
    @JsonIgnore
    @ToString.Exclude
    private AvtaleInnhold avtaleInnhold;

    public Inkluderingstilskudd() {

    }

    public Inkluderingstilskudd(Inkluderingstilskudd fra) {
        id = UUID.randomUUID();
        beløp = fra.beløp;
        type = fra.type;
        forklaring = fra.forklaring;
    }
}
