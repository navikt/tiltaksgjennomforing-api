package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MidlertidigLonnstilskudd extends Avtale {
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private String stillingprosent;
    private Integer lonnstilskuddProsent;
    private LocalDate lonnstilskuddStartdato;
    private LocalDate lonnstilskuddEvalueringsdato;
    private String manedslonn;
    private String feriepengesats;
    private String arbeidsgiveravgift;

    public MidlertidigLonnstilskudd(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent);
    }

    @Override
    boolean heleAvtalenErFyltUt() {
        return super.heleAvtalenErFyltUt() && Utils.erIkkeTomme(arbeidsgiverKontonummer,
                stillingtype,
                stillingbeskrivelse,
                stillingprosent,
                lonnstilskuddProsent,
                lonnstilskuddStartdato,
                lonnstilskuddEvalueringsdato,
                manedslonn,
                feriepengesats,
                arbeidsgiveravgift);
    }

    @Override
    public String status() {
        if (isAvbrutt()) {
            return "Avbrutt";
        } else if (erGodkjentAvVeileder() && lonnstilskuddEvalueringsdato.isAfter(LocalDate.now())) {
            return "Avsluttet";
        } else if (erGodkjentAvVeileder()) {
            return "Klar for oppstart";
        } else if (heleAvtalenErFyltUt()) {
            return "Mangler godkjenning";
        } else {
            return "Påbegynt";
        }
    }

    @Override
    public Tiltakstype tiltakstype() {
        return Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
    }
}
