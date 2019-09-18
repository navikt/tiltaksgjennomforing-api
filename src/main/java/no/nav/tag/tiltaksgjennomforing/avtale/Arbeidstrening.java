package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Arbeidstrening extends Avtale {
    private LocalDate startDato;
    private Integer arbeidstreningLengde;
    private Integer arbeidstreningStillingprosent;
    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Maal> maal = new ArrayList<>();
    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oppgave> oppgaver = new ArrayList<>();

    public Arbeidstrening(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        super(deltakerFnr, bedriftNr, veilederNavIdent);
    }

    @Override
    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale, Avtalerolle utfortAv) {
        super.endreAvtale(versjon, nyAvtale, utfortAv);
        setStartDato(nyAvtale.getStartDato());
        setArbeidstreningLengde(nyAvtale.getArbeidstreningLengde());
        setArbeidstreningStillingprosent(nyAvtale.getArbeidstreningStillingprosent());

        maal.clear();
        maal.addAll(nyAvtale.getMaal());
        maal.forEach(m -> m.setAvtale(this));

        oppgaver.clear();
        oppgaver.addAll(nyAvtale.getOppgaver());
        oppgaver.forEach(o -> o.setAvtale(this));
    }

    @Override
    boolean heleAvtalenErFyltUt() {
        return super.heleAvtalenErFyltUt() && Utils.erIkkeTomme(startDato, arbeidstreningLengde, arbeidstreningStillingprosent) && !oppgaver.isEmpty() && !maal.isEmpty();
    }

    @JsonProperty("status")
    public String status() {
        if (isAvbrutt()) {
            return "Avbrutt";
        } else if (erGodkjentAvVeileder() && (startDato.plusWeeks(arbeidstreningLengde).isBefore(LocalDate.now()))) {
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
        return Tiltakstype.ARBEIDSTRENING;
    }
}
