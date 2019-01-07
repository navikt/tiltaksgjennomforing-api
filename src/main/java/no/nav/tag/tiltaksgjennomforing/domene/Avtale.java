package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.Utils.ikkeNull;

@Data
public class Avtale {

    private final LocalDateTime opprettetTidspunkt;
    private final Fnr deltakerFnr;
    private final NavIdent veilederNavIdent;
    @Id
    private Integer id;
    private String versjon;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String deltakerAdresse;
    private String deltakerPostnummer;
    private String deltakerPoststed;
    private String bedriftNavn;
    private String bedriftAdresse;
    private String bedriftPostnummer;
    private String bedriftPoststed;
    private Fnr arbeidsgiverFnr;
    private String arbeidsgiverFornavn;
    private String arbeidsgiverEtternavn;
    private String arbeidsgiverEpost;
    private String arbeidsgiverTlf;
    private String veilederFornavn;
    private String veilederEtternavn;
    private String veilederEpost;
    private String veilederTlf;

    private String oppfolging;
    private String tilrettelegging;

    private LocalDateTime startDatoTidspunkt;
    private Integer arbeidstreningLengde;
    private Integer arbeidstreningStillingprosent;

    @Column(keyColumn = "id")
    private List<Maal> maal = new ArrayList<>();
    @Column(keyColumn = "id")
    private List<Oppgave> oppgaver = new ArrayList<>();

    private boolean bekreftetAvBruker;
    private boolean bekreftetAvArbeidsgiver;
    private boolean bekreftetAvVeileder;

    @PersistenceConstructor
    public Avtale(Fnr deltakerFnr, NavIdent veilederNavIdent, LocalDateTime opprettetTidspunkt) {
        this.deltakerFnr = ikkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.veilederNavIdent = ikkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
        this.opprettetTidspunkt = ikkeNull(opprettetTidspunkt, "Opprettet tidspunkt må være satt.");
    }

    public boolean kanOpprettesAv(Person person) {
        if (person instanceof Veileder) {
            return erTilgjengeligFor(person);
        }
        return false;
    }

    public boolean erTilgjengeligFor(Person person) {
        if (person instanceof Veileder) {
            return veilederNavIdent.equals(((Veileder) person).getNavIdent());
        } else if (person instanceof Bruker) {
            Fnr fnr = ((Bruker) person).getFnr();
            return fnr.equals(arbeidsgiverFnr) || fnr.equals(deltakerFnr);
        }
        return false;
    }

    public static Avtale nyAvtale(OpprettAvtale opprettAvtale) {
        Avtale avtale = new Avtale(opprettAvtale.getDeltakerFnr(), opprettAvtale.getVeilederNavIdent(), LocalDateTime.now());
        avtale.setVersjon("1");
        return avtale;
    }

    public void endreAvtale(EndreAvtale nyAvtale) {
        inkrementerVersjonsnummer();

        setDeltakerFornavn(nyAvtale.getDeltakerFornavn());
        setDeltakerEtternavn(nyAvtale.getDeltakerEtternavn());
        setDeltakerAdresse(nyAvtale.getDeltakerAdresse());
        setDeltakerPostnummer(nyAvtale.getDeltakerPostnummer());
        setDeltakerPoststed(nyAvtale.getDeltakerPoststed());

        setBedriftNavn(nyAvtale.getBedriftNavn());
        setBedriftAdresse(nyAvtale.getBedriftAdresse());
        setBedriftPostnummer(nyAvtale.getBedriftPostnummer());
        setBedriftPoststed(nyAvtale.getBedriftPoststed());

        setArbeidsgiverFnr(nyAvtale.getArbeidsgiverFnr());
        setArbeidsgiverFornavn(nyAvtale.getArbeidsgiverFornavn());
        setArbeidsgiverEtternavn(nyAvtale.getArbeidsgiverEtternavn());
        setArbeidsgiverEpost(nyAvtale.getArbeidsgiverEpost());
        setArbeidsgiverTlf(nyAvtale.getArbeidsgiverTlf());

        setVeilederFornavn(nyAvtale.getVeilederFornavn());
        setVeilederEtternavn(nyAvtale.getVeilederEtternavn());
        setVeilederEpost(nyAvtale.getVeilederEpost());
        setVeilederTlf(nyAvtale.getVeilederTlf());

        setOppfolging(nyAvtale.getOppfolging());
        setTilrettelegging(nyAvtale.getTilrettelegging());
        setStartDatoTidspunkt(nyAvtale.getStartDatoTidspunkt());
        setArbeidstreningLengde(nyAvtale.getArbeidstreningLengde());
        setArbeidstreningStillingprosent(nyAvtale.getArbeidstreningStillingprosent());

        setMaal(nyAvtale.getMaal());
        maal.forEach(Maal::setterOppretterTidspunkt);
        setOppgaver(nyAvtale.getOppgaver());
        oppgaver.forEach(Oppgave::setterOppretterTidspunkt);

        setBekreftetAvBruker(nyAvtale.isBekreftetAvBruker());
        setBekreftetAvArbeidsgiver(nyAvtale.isBekreftetAvArbeidsgiver());
        setBekreftetAvVeileder(nyAvtale.isBekreftetAvVeileder());
    }

    private void inkrementerVersjonsnummer() {
        int versjonsnummer = Integer.parseInt(this.versjon);
        versjon = String.valueOf(versjonsnummer + 1);
    }

    public void sjekkVersjon(String versjon) {
        if (!this.versjon.equals(versjon)) {
            throw new TiltaksgjennomforingException("Ugyldig versjonsnummer, kan ikke endre avtale.");
        }
    }
}
