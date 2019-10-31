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

        maal.clear();
        maal.addAll(nyAvtale.getMaal());
        maal.forEach(m -> m.setAvtale(this));

        oppgaver.clear();
        oppgaver.addAll(nyAvtale.getOppgaver());
        oppgaver.forEach(o -> o.setAvtale(this));
    }

    @Override
    boolean heleAvtalenErFyltUt() {
        return super.heleAvtalenErFyltUt() && !oppgaver.isEmpty() && !maal.isEmpty();
    }

    @Override
    public Tiltakstype tiltakstype() {
        return Tiltakstype.ARBEIDSTRENING;
    }
}
