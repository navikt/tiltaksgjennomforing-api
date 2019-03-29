package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.AltinnService;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.Utils.erIkkeNull;
import static no.nav.tag.tiltaksgjennomforing.domene.Utils.sjekkAtIkkeNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class Avtale extends AbstractAggregateRoot {

    private final Fnr deltakerFnr;
    private final NavIdent veilederNavIdent;
    @JsonIgnore
    private final Fnr arbeidsgiverFnr;
    private LocalDateTime opprettetTidspunkt;
    @Id
    private UUID id;
    private Integer versjon;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String bedriftNavn;
    private BedriftNr bedriftNr;
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
        avtale.registerEvent(new AvtaleOpprettet(avtale, veilederNavIdent));
        return avtale;
    }

    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale, Avtalerolle utfortAv) {
        sjekkOmAvtalenKanEndres();
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

        registerEvent(new AvtaleEndret(this, utfortAv));
    }

    @JsonProperty("erLaast")
    public boolean erLaast() {
        return godkjentAvVeileder && godkjentAvArbeidsgiver && godkjentAvDeltaker;
    }

    private void sjekkOmAvtalenKanEndres() {
        if (godkjentAvDeltaker || godkjentAvArbeidsgiver || godkjentAvVeileder) {
            throw new TilgangskontrollException("Godkjenninger må oppheves før avtalen kan endres.");
        }
    }

    void opphevGodkjenninger(Avtalerolle avtalerolle) {
        setGodkjentAvDeltaker(false);
        setGodkjentAvArbeidsgiver(false);
        setGodkjentAvVeileder(false);
        registerEvent(new GodkjenningerOpphevet(this, avtalerolle));
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
        }
        else {
            throw new TilgangskontrollException("Er ikke part i avtalen");
        }
    }

    void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvArbeidsgiver = true;
        registerEvent(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    void godkjennForVeileder(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvVeileder = true;
        registerEvent(new GodkjentAvVeileder(this, utfortAv));
    }

    void godkjennForDeltaker(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvDeltaker = true;
        registerEvent(new GodkjentAvDeltaker(this, utfortAv));
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
