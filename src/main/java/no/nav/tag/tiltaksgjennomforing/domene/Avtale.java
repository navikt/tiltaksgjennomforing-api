package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.controller.TilgangskontrollException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.Utils.ikkeNull;
import static no.nav.tag.tiltaksgjennomforing.domene.Rolle.*;

@Data
public class Avtale {

    private LocalDateTime opprettetTidspunkt;
    private final Fnr deltakerFnr;
    private final NavIdent veilederNavIdent;
    @Id
    private UUID id;
    private Integer versjon;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String deltakerAdresse;
    private String deltakerPostnummer;
    private String deltakerPoststed;
    private String bedriftNavn;
    private String bedriftAdresse;
    private String bedriftPostnummer;
    private String bedriftPoststed;
    private final Fnr arbeidsgiverFnr;
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

    private boolean godkjentAvDeltaker;
    private boolean godkjentAvArbeidsgiver;
    private boolean godkjentAvVeileder;

    @PersistenceConstructor
    public Avtale(Fnr deltakerFnr, Fnr arbeidsgiverFnr, NavIdent veilederNavIdent) {
        this.deltakerFnr = ikkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.arbeidsgiverFnr = ikkeNull(arbeidsgiverFnr, "Arbeidsgivers fnr må være satt.");
        this.veilederNavIdent = ikkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
    }

    public static Avtale nyAvtale(OpprettAvtale opprettAvtale, NavIdent veilederNavIdent) {
        Avtale avtale = new Avtale(opprettAvtale.getDeltakerFnr(), opprettAvtale.getArbeidsgiverFnr(), veilederNavIdent);
        avtale.setVersjon(1);
        return avtale;
    }

    public void endreAvtale(Integer versjon, Person personSomEndrer, EndreAvtale nyAvtale) {
        sjekkOmKanEndreAvtale(personSomEndrer);
        sjekkVersjon(versjon);
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
        setOppgaver(nyAvtale.getOppgaver());
    }

    public boolean erTilgjengeligFor(Person person) {
        PersonIdentifikator id = person.getIdentifikator();

        return id.equals(veilederNavIdent) ||
                id.equals(deltakerFnr) ||
                id.equals(arbeidsgiverFnr);
    }

    private void sjekkOmKanEndreAvtale(Person person) {
        Rolle rolle = hentRollenTil(person);
        if (rolle != ARBEIDSGIVER && rolle != VEILEDER) {
            throw new TilgangskontrollException("Kun arbeidsgiver og veileder kan endre på avtalen.");
        }
    }

    private void inkrementerVersjonsnummer() {
        versjon += 1;
    }

    public void sjekkVersjon(Integer versjon) {
        if (this.versjon != versjon) {
            throw new TiltaksgjennomforingException("Avtalen kan ikke lagres, versjonen er utdatert.");
        }
    }

    public void settIdOgOpprettetTidspunkt() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }

        if (this.getOpprettetTidspunkt() == null) {
            this.opprettetTidspunkt = LocalDateTime.now();
        }

        this.getMaal().forEach(Maal::settIdOgOpprettetTidspunkt);
        this.getOppgaver().forEach(Oppgave::settIdOgOpprettetTidspunkt);
    }

    public Rolle hentRollenTil(Person person) {
        if (person.getIdentifikator().equals(deltakerFnr)) {
            return DELTAKER;
        } else if (person.getIdentifikator().equals(arbeidsgiverFnr)) {
            return ARBEIDSGIVER;
        } else if (person.getIdentifikator().equals(veilederNavIdent)) {
            return VEILEDER;
        } else {
            throw new TilgangskontrollException("Brukeren er ikke tilknyttet avtalen.");
        }
    }

    public void endreGodkjenningDeltaker(Person personSomGodkjenner, boolean verdi) {
        Rolle rolle = hentRollenTil(personSomGodkjenner);
        if (rolle.equals(DELTAKER)) {
            setGodkjentAvDeltaker(verdi);
        } else {
            throw new TilgangskontrollException("Kun deltaker kan godkjenne på vegne av seg selv.");
        }
    }

    public void endreGodkjenningArbeidsgiver(Person personSomGodkjenner, boolean verdi) {
        Rolle rolle = hentRollenTil(personSomGodkjenner);
        if (rolle.equals(ARBEIDSGIVER)) {
            setGodkjentAvArbeidsgiver(verdi);
        } else {
            throw new TilgangskontrollException("Kun arbeidsgiver kan godkjenne på vegne av seg selv.");
        }
    }

    public void endreGodkjenningVeileder(Person personSomGodkjenner, boolean verdi) {
        Rolle rolle = hentRollenTil(personSomGodkjenner);
        if (rolle.equals(VEILEDER)) {
            setGodkjentAvVeileder(verdi);
        } else {
            throw new TilgangskontrollException("Kun veileder kan godkjenne på vegne av seg selv.");
        }
    }
}
