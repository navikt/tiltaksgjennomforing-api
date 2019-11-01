package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD_VERDI)
@NoArgsConstructor
public class MidlertidigLonnstilskudd extends Lonnstilskudd {
    public MidlertidigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }
}
