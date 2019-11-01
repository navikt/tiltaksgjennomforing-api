package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(Tiltakstype.VARIG_LONNSTILSKUDD_VERDI)
@NoArgsConstructor
public class VarigLonnstilskudd extends Lonnstilskudd {
    public VarigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent, Tiltakstype.VARIG_LONNSTILSKUDD);
    }
}
