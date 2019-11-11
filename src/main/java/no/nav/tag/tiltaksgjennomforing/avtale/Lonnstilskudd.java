package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.Entity;
import java.math.BigDecimal;


@NoArgsConstructor
@Data
@Entity
public abstract class Lonnstilskudd extends Avtale {
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;

    public Lonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent, Tiltakstype tiltakstype) {
        super(deltakerFnr, bedriftNr, veilederNavIdent, tiltakstype);
    }

    @Override
    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale, Avtalerolle utfortAv) {
        setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
        setStillingtype(nyAvtale.getStillingtype());
        setStillingbeskrivelse(nyAvtale.getStillingbeskrivelse());
        setLonnstilskuddProsent(nyAvtale.getLonnstilskuddProsent());
        setManedslonn(nyAvtale.getManedslonn());
        setFeriepengesats(nyAvtale.getFeriepengesats());
        setArbeidsgiveravgift(nyAvtale.getArbeidsgiveravgift());
        super.endreAvtale(versjon, nyAvtale, utfortAv);
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
