package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@DiscriminatorValue(Tiltakstype.VARIG_LONNSTILSKUDD_VERDI)
@NoArgsConstructor
public class VarigLonnstilskudd extends Lonnstilskudd {
    public VarigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent, Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Override
    void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null) {
            if (startDato.isAfter(sluttDato)) {
                throw new StartDatoErEtterSluttDatoException();
            }
        }
    }
}
