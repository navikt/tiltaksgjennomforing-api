package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;

@NoArgsConstructor
public abstract class Lonnstilskudd extends Avtale {
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private Integer lonnstilskuddProsent;
    private String manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;

    public Lonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent, Tiltakstype tiltakstype) {
        super(deltakerFnr, bedriftNr, veilederNavIdent, tiltakstype);
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
