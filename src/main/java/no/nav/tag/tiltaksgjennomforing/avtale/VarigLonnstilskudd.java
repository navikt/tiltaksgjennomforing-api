package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VarigLonnstilskudd extends Avtale {
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private Integer lonnstilskuddProsent;
    private String manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;

    public VarigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent);
    }

    @Override
    boolean heleAvtalenErFyltUt() {
        return super.heleAvtalenErFyltUt() && Utils.erIkkeTomme(arbeidsgiverKontonummer,
                stillingtype,
                stillingbeskrivelse,
                lonnstilskuddProsent,
                manedslonn,
                feriepengesats,
                arbeidsgiveravgift);
    }

    @Override
    public Tiltakstype tiltakstype() {
        return Tiltakstype.VARIG_LONNSTILSKUDD;
    }
}
