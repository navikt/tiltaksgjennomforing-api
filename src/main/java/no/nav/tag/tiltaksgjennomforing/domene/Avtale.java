package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.GamleVerdier;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.domene.Utils.sjekkAtIkkeNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class Avtale extends AbstractAggregateRoot {

    private final Fnr deltakerFnr;
    private final BedriftNr bedriftNr;
    private final NavIdent veilederNavIdent;

    private LocalDateTime opprettetTidspunkt;
    @Id
    private UUID id;
    private Integer versjon;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String deltakerTlf;
    private String bedriftNavn;
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

    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;

    private LocalDateTime godkjentAvDeltaker;
    private LocalDateTime godkjentAvArbeidsgiver;
    private LocalDateTime godkjentAvVeileder;
    private boolean godkjentPaVegneAv;
    private boolean avbrutt;

    @PersistenceConstructor
    public Avtale(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        this.deltakerFnr = sjekkAtIkkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.bedriftNr = sjekkAtIkkeNull(bedriftNr, "Arbeidsgivers bedriftnr må være satt.");
        this.veilederNavIdent = sjekkAtIkkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
    }

    public static Avtale nyAvtale(OpprettAvtale opprettAvtale, NavIdent veilederNavIdent) {
        Avtale avtale = new Avtale(opprettAvtale.getDeltakerFnr(), opprettAvtale.getBedriftNr(), veilederNavIdent);
        avtale.setVersjon(1);
        avtale.registerEvent(new AvtaleOpprettet(avtale, veilederNavIdent));
        return avtale;
    }

    private static void sjekkMaalOgOppgaverLengde(List<Maal> maal, List<Oppgave> oppgaver) {
        maal.forEach(Maal::sjekkMaalLengde);
        oppgaver.forEach(Oppgave::sjekkOppgaveLengde);
    }

    public void endreAvtale(Integer versjon, EndreAvtale nyAvtale, Avtalerolle utfortAv) {
        sjekkOmAvtalenKanEndres();
        sjekkVersjon(versjon);
        inkrementerVersjonsnummer();
        sjekkMaalOgOppgaverLengde(nyAvtale.getMaal(), nyAvtale.getOppgaver());

        setDeltakerFornavn(nyAvtale.getDeltakerFornavn());
        setDeltakerEtternavn(nyAvtale.getDeltakerEtternavn());
        setDeltakerTlf(nyAvtale.getDeltakerTlf());

        setBedriftNavn(nyAvtale.getBedriftNavn());

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
        return erGodkjentAvVeileder() && erGodkjentAvArbeidsgiver() && erGodkjentAvDeltaker();
    }

    public boolean erGodkjentAvDeltaker() {
        return godkjentAvDeltaker != null;
    }

    public boolean erGodkjentAvArbeidsgiver() {
        return godkjentAvArbeidsgiver != null;
    }

    public boolean erGodkjentAvVeileder() {
        return godkjentAvVeileder != null;
    }

    private void sjekkOmAvtalenKanEndres() {
        if (erGodkjentAvDeltaker() || erGodkjentAvArbeidsgiver() || erGodkjentAvVeileder()) {
            throw new TilgangskontrollException("Godkjenninger må oppheves før avtalen kan endres.");
        }
    }

    void opphevGodkjenningerSomArbeidsgiver() {
        boolean varGodkjentAvDeltaker = erGodkjentAvDeltaker();
        opphevGodkjenninger();
        registerEvent(new GodkjenningerOpphevetAvArbeidsgiver(this, new GamleVerdier(varGodkjentAvDeltaker, false)));
    }

    void opphevGodkjenningerSomVeileder() {
        boolean varGodkjentAvDeltaker = erGodkjentAvDeltaker();
        boolean varGodkjentAvArbeidsgiver = erGodkjentAvArbeidsgiver();
        opphevGodkjenninger();
        registerEvent(new GodkjenningerOpphevetAvVeileder(this, new GamleVerdier(varGodkjentAvDeltaker, varGodkjentAvArbeidsgiver)));
    }

    private void opphevGodkjenninger() {
        setGodkjentAvDeltaker(null);
        setGodkjentAvArbeidsgiver(null);
        setGodkjentAvVeileder(null);
        setGodkjentPaVegneAv(false);
        setGodkjentPaVegneGrunn(null);
    }

    private void inkrementerVersjonsnummer() {
        versjon += 1;
    }

    void sjekkVersjon(Integer versjon) {
        if (versjon == null || !versjon.equals(this.versjon)) {
            throw new SamtidigeEndringerException("Du må oppdatere siden før du kan lagre eller godkjenne. Det er gjort endringer i avtalen som du ikke har sett.");
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

    void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvArbeidsgiver = LocalDateTime.now();
        registerEvent(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    void godkjennForVeileder(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvVeileder = LocalDateTime.now();
        registerEvent(new GodkjentAvVeileder(this, utfortAv));
    }

    void godkjennForVeilederOgDeltaker(Identifikator utfortAv, GodkjentPaVegneGrunn paVegneAvGrunn) {
        sjekkOmKanGodkjennes();
        this.godkjentAvVeileder = LocalDateTime.now();
        this.godkjentAvDeltaker = LocalDateTime.now();
        this.godkjentPaVegneAv = true;
        this.godkjentPaVegneGrunn = paVegneAvGrunn;
        registerEvent(new GodkjentPaVegneAv(this, utfortAv));
    }

    void godkjennForDeltaker(Identifikator utfortAv) {
        sjekkOmKanGodkjennes();
        this.godkjentAvDeltaker = LocalDateTime.now();
        registerEvent(new GodkjentAvDeltaker(this, utfortAv));
    }

    void sjekkOmKanGodkjennes() {
        if (!heleAvtalenErFyltUt()) {
            throw new TiltaksgjennomforingException("Alt må være utfylt før avtalen kan godkjennes.");
        }
    }

    @JsonProperty("status")
    public String status() {
        if (avbrutt) {
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

    @JsonProperty("kanAvbrytes")
    public boolean kanAvbrytes() {
        // Nå regner vi at veileder kan avbryte avtalen hvis veileder ikke har godkjent(kan også være at han kan
        // avbryte kun de avtalene som ikke er godkjente av deltaker og AG),
        return !erGodkjentAvVeileder() && !isAvbrutt();
    }

    public void avbryt(Veileder veileder) {
        if (this.kanAvbrytes()) {
            this.setAvbrutt(true);
            registerEvent(new AvbruttAvVeileder(this, veileder.getIdentifikator()));
        }
    }

    private boolean heleAvtalenErFyltUt() {
        return erIkkeTomme(deltakerFnr,
                veilederNavIdent,
                deltakerFornavn,
                deltakerEtternavn,
                deltakerTlf,
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
