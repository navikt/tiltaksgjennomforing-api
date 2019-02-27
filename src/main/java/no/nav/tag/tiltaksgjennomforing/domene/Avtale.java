package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.Utils.erIkkeNull;
import static no.nav.tag.tiltaksgjennomforing.Utils.sjekkAtIkkeNull;

@Data
public class Avtale {

    private final Fnr deltakerFnr;
    private final NavIdent veilederNavIdent;
    private final Fnr arbeidsgiverFnr;
    private LocalDateTime opprettetTidspunkt;
    @Id
    private UUID id;
    private Integer versjon;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String bedriftNavn;
    private String bedriftNr;
    private String arbeidsgiverFornavn;
    private String arbeidsgiverEtternavn;
    private String arbeidsgiverTlf;
    private String veilederFornavn;
    private String veilederEtternavn;
    private String veilederTlf;

    private String oppfolging;
    private String tilrettelegging;

    private LocalDate startDato;
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
    public Avtale(Fnr deltakerFnr, Fnr arbeidsgiverFnr, String bedriftNavn, NavIdent veilederNavIdent) {
        this.deltakerFnr = sjekkAtIkkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.arbeidsgiverFnr = sjekkAtIkkeNull(arbeidsgiverFnr, "Arbeidsgivers fnr må være satt.");
        this.veilederNavIdent = sjekkAtIkkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
        this.bedriftNavn = bedriftNavn;
    }

    public static Avtale nyAvtale(OpprettAvtale opprettAvtale, NavIdent veilederNavIdent) {
        Avtale avtale = new Avtale(opprettAvtale.getDeltakerFnr(), opprettAvtale.getArbeidsgiverFnr(), opprettAvtale.getBedriftNavn(), veilederNavIdent);
        avtale.setVersjon(1);
        return avtale;
    }

    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale) {
        sjekkVersjon(versjon);
        inkrementerVersjonsnummer();

        setDeltakerFornavn(nyAvtale.getDeltakerFornavn());
        setDeltakerEtternavn(nyAvtale.getDeltakerEtternavn());

        setBedriftNavn(nyAvtale.getBedriftNavn());
        setBedriftNr(nyAvtale.getBedriftNr());

        setArbeidsgiverFornavn(nyAvtale.getArbeidsgiverFornavn());
        setArbeidsgiverEtternavn(nyAvtale.getArbeidsgiverEtternavn());
        setArbeidsgiverTlf(nyAvtale.getArbeidsgiverTlf());

        setVeilederFornavn(nyAvtale.getVeilederFornavn());
        setVeilederEtternavn(nyAvtale.getVeilederEtternavn());
        setVeilederTlf(nyAvtale.getVeilederTlf());

        setOppfolging(nyAvtale.getOppfolging());
        setTilrettelegging(nyAvtale.getTilrettelegging());
        setStartDato(nyAvtale.getStartDato());
        setArbeidstreningLengde(nyAvtale.getArbeidstreningLengde());
        setArbeidstreningStillingprosent(nyAvtale.getArbeidstreningStillingprosent());

        setMaal(nyAvtale.getMaal());
        setOppgaver(nyAvtale.getOppgaver());
    }

    public void sjekkLesetilgang(InnloggetBruker bruker) {
        if (!harLesetilgang(bruker)) {
            throw new TilgangskontrollException("Innlogget bruker har ikke lesetilgang til avtalen.");
        }
    }

    public boolean harLesetilgang(InnloggetBruker bruker) {
        Identifikator id = bruker.getIdentifikator();

        return id.equals(veilederNavIdent) ||
                id.equals(deltakerFnr) ||
                id.equals(arbeidsgiverFnr);
    }

    private void inkrementerVersjonsnummer() {
        versjon += 1;
    }

    void sjekkVersjon(Integer versjon) {
        if (versjon == null || !versjon.equals(this.versjon)) {
            throw new SamtidigeEndringerException("Noen andre har lagret avtalen.");
        }
    }

    void settIdOgOpprettetTidspunkt() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }

        if (this.getOpprettetTidspunkt() == null) {
            this.opprettetTidspunkt = LocalDateTime.now();
        }

        this.getMaal().forEach(Maal::settIdOgOpprettetTidspunkt);
        this.getOppgaver().forEach(Oppgave::settIdOgOpprettetTidspunkt);
    }

    public Avtalepart hentAvtalepart(InnloggetBruker innloggetBruker) {
        Identifikator identifikator = innloggetBruker.getIdentifikator();
        if (identifikator.equals(deltakerFnr)) {
            return new Deltaker(deltakerFnr, this);
        } else if (identifikator.equals(arbeidsgiverFnr)) {
            return new Arbeidsgiver(arbeidsgiverFnr, this);
        } else if (identifikator.equals(veilederNavIdent)) {
            return new Veileder(veilederNavIdent, this);
        } else {
            throw new TilgangskontrollException("Er ikke part i avtalen");
        }
    }

    void endreArbeidsgiversGodkjennelse(boolean godkjennelse) {
        sjekkOmKanGodkjennes();
        this.godkjentAvArbeidsgiver = godkjennelse;
    }

    void endreVeiledersGodkjennelse(boolean godkjennelse) {
        sjekkOmKanGodkjennes();
        this.godkjentAvVeileder = godkjennelse;
    }

    void endreDeltakersGodkjennelse(boolean godkjennelse) {
        sjekkOmKanGodkjennes();
        this.godkjentAvDeltaker = godkjennelse;
    }

    void sjekkOmKanGodkjennes() {
        if (!heleAvtalenErFyltUt()) {
            throw new TiltaksgjennomforingException("Alt må være utfylt før avtalen kan godkjennes.");
        }
    }

    private boolean heleAvtalenErFyltUt() {
        return erIkkeNull(deltakerFnr,
                veilederNavIdent,
                arbeidsgiverFnr,
                deltakerFornavn,
                deltakerEtternavn,
                bedriftNavn,
                arbeidsgiverFornavn,
                arbeidsgiverEtternavn,
                arbeidsgiverTlf,
                veilederFornavn,
                veilederEtternavn,
                veilederTlf,
                oppfolging,
                tilrettelegging,
                startDato,
                arbeidstreningLengde,
                arbeidstreningStillingprosent
        )
                && !oppgaver.isEmpty() && !maal.isEmpty();
    }
}
