package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD_VERDI)
public class MidlertidigLonnstilskudd extends Avtale {
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private Integer lonnstilskuddProsent;
    private String manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;

    public MidlertidigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent);
        setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    @Override
    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale, Avtalerolle utfortAv) {
        super.endreAvtale(versjon, nyAvtale, utfortAv);
        arbeidsgiverKontonummer = nyAvtale.getArbeidsgiverKontonummer();
        stillingtype = nyAvtale.getStillingtype();
        stillingbeskrivelse = nyAvtale.getStillingbeskrivelse();
        lonnstilskuddProsent = nyAvtale.getLonnstilskuddProsent();
        manedslonn = nyAvtale.getManedslonn();
        feriepengesats = nyAvtale.getFeriepengesats();
        arbeidsgiveravgift = nyAvtale.getArbeidsgiveravgift();
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
}
