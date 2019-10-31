package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.AvtalensVarighetMerEnnMaksimaltAntallMånederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.sjekkAtIkkeNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public class Avtale extends AbstractAggregateRoot<Avtale> {
    private static final int MAKSIMALT_ANTALL_MÅNEDER_VARIGHET = 3;

    @Convert(converter = FnrConverter.class)
    private Fnr deltakerFnr;
    @Convert(converter = BedriftNrConverter.class)
    private BedriftNr bedriftNr;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent veilederNavIdent;

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
    private String journalpostId;

    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer stillingprosent;

    @OneToOne(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true)
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;

    private LocalDateTime godkjentAvDeltaker;
    private LocalDateTime godkjentAvArbeidsgiver;
    private LocalDateTime godkjentAvVeileder;
    private boolean godkjentPaVegneAv;
    private boolean avbrutt;

    public Avtale(Fnr deltakerFnr, BedriftNr bedriftNr, NavIdent veilederNavIdent) {
        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = LocalDateTime.now();
        this.deltakerFnr = sjekkAtIkkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.bedriftNr = sjekkAtIkkeNull(bedriftNr, "Arbeidsgivers bedriftnr må være satt.");
        this.veilederNavIdent = sjekkAtIkkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
        this.versjon = 1;
        registerEvent(new AvtaleOpprettet(this, veilederNavIdent));
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
        sjekkStartOgSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());

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
        setSluttDato(nyAvtale.getSluttDato());
        setStillingprosent(nyAvtale.getStillingprosent());

        registerEvent(new AvtaleEndret(this, utfortAv));
    }

    private static void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null) {
            if (startDato.isAfter(sluttDato)) {
                throw new StartDatoErEtterSluttDatoException();
            } else if (sluttDato.isAfter(startDato.plusMonths(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET))) {
                throw new AvtalensVarighetMerEnnMaksimaltAntallMånederException(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET);
            }
        }
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
        paVegneAvGrunn.setId(this.id);
        paVegneAvGrunn.setAvtale(this);
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
        if (isAvbrutt()) {
            return "Avbrutt";
        } else if (erGodkjentAvVeileder() && (sluttDato.isBefore(LocalDate.now()))) {
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

    boolean heleAvtalenErFyltUt() {
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
                sluttDato,
                stillingprosent
        );
    }

    @JsonProperty("tiltakstype")
    public Tiltakstype tiltakstype() {
        return null;
    }
}
