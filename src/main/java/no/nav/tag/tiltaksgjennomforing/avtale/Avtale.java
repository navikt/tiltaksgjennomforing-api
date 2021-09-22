package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy.StartOgSluttDatoStrategyFactory;
import no.nav.tag.tiltaksgjennomforing.exceptions.*;
import no.nav.tag.tiltaksgjennomforing.persondata.Navn;
import no.nav.tag.tiltaksgjennomforing.persondata.NavnFormaterer;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.TelefonnummerValidator;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.VarselType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.Notifikasjon;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.NotifikasjonerProperties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.sjekkAtIkkeNull;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@FieldNameConstants
public class Avtale extends AbstractAggregateRoot<Avtale> {

    @Convert(converter = FnrConverter.class)
    private Fnr deltakerFnr;
    @Convert(converter = BedriftNrConverter.class)
    private BedriftNr bedriftNr;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent veilederNavIdent;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private Tiltakstype tiltakstype;

    private LocalDateTime opprettetTidspunkt;
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Generated(GenerationTime.INSERT)
    private Integer avtaleNr;

    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy(AvtaleInnhold.Fields.versjon)
    private List<AvtaleInnhold> versjoner = new ArrayList<>();

    private Instant sistEndret;
    private Instant annullertTidspunkt;
    private String annullertGrunn;
    private boolean avbrutt;
    private boolean slettemerket;
    private LocalDate avbruttDato;
    private String avbruttGrunn;
    private boolean opprettetAvArbeidsgiver;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;


    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @SortNatural
    private SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    private boolean feilregistrert;



    private Avtale(OpprettAvtale opprettAvtale) {
        sjekkAtIkkeNull(opprettAvtale.getDeltakerFnr(), "Deltakers fnr må være satt.");
        sjekkAtIkkeNull(opprettAvtale.getBedriftNr(), "Arbeidsgivers bedriftnr må være satt.");
        if (opprettAvtale.getDeltakerFnr().erUnder16år()) {
            throw new FeilkodeException(Feilkode.IKKE_GAMMEL_NOK);
        }
        if (opprettAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && opprettAvtale.getDeltakerFnr().erOver30år()) {
            throw new FeilkodeException(Feilkode.FOR_GAMMEL);
        }
        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = LocalDateTime.now();
        this.deltakerFnr = opprettAvtale.getDeltakerFnr();
        this.bedriftNr = opprettAvtale.getBedriftNr();
        this.tiltakstype = opprettAvtale.getTiltakstype();
        this.sistEndret = Instant.now();
        var innhold = AvtaleInnhold.nyttTomtInnhold(tiltakstype);
        innhold.setAvtale(this);
        this.versjoner.add(innhold);
    }

    public static Avtale veilederOppretterAvtale(OpprettAvtale opprettAvtale, NavIdent navIdent) {
        Avtale avtale = new Avtale(opprettAvtale);
        avtale.veilederNavIdent = sjekkAtIkkeNull(navIdent, "Veileders NAV-ident må være satt.");
        avtale.registerEvent(new AvtaleOpprettetAvVeileder(avtale, navIdent));
        return avtale;
    }

    public static Avtale arbeidsgiverOppretterAvtale(OpprettAvtale opprettAvtale) {
        Avtale avtale = new Avtale(opprettAvtale);
        avtale.opprettetAvArbeidsgiver = true;
        avtale.registerEvent(new AvtaleOpprettetAvArbeidsgiver(avtale));
        return avtale;
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale nyAvtale, Avtalerolle utfortAv, EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAvtalenKanEndres();
        sjekkSistEndret(sistEndret);
        sjekkStartOgSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        gjeldendeInnhold().endreAvtale(nyAvtale);
        if (tiltakstyperMedTilskuddsperioder.contains(tiltakstype)) {
            nyeTilskuddsperioder();
        }
        sistEndretNå();
        registerEvent(new AvtaleEndret(this, utfortAv));
    }

    public void delMedAvtalepart(Avtalerolle avtalerolle) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        String tlf = telefonnummerTilAvtalepart(avtalerolle);
        if (!TelefonnummerValidator.erGyldigMobilnummer(tlf)) {
            throw new FeilkodeException(Feilkode.UGYLDIG_TLF);
        }
        registerEvent(new AvtaleDeltMedAvtalepart(this, avtalerolle));
    }

    public void refusjonKlar(VarselType varselType) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (varselType == VarselType.KLAR) {
            registerEvent(new RefusjonKlar(this));
        } else if (varselType == VarselType.REVARSEL) {
            registerEvent(new RefusjonKlarRevarsel(this));
        }
    }

    public void refusjonFristForlenget() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        registerEvent(new RefusjonFristForlenget(this));
    }

    private String telefonnummerTilAvtalepart(Avtalerolle avtalerolle) {
        switch (avtalerolle) {
            case DELTAKER:
                return getDeltakerTlf();
            case ARBEIDSGIVER:
                return getArbeidsgiverTlf();
            case VEILEDER:
                return getVeilederTlf();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Delegate(excludes = MetoderSomIkkeSkalDelegeresFraAvtaleInnhold.class)
    public AvtaleInnhold gjeldendeInnhold() {
        return versjoner.get(versjoner.size() - 1);
    }

    @JsonProperty
    public boolean erLaast() {
        return erGodkjentAvVeileder() && erGodkjentAvArbeidsgiver() && erGodkjentAvDeltaker();
    }

    @JsonProperty
    public boolean erGodkjentAvDeltaker() {
        return this.getGodkjentAvDeltaker() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvArbeidsgiver() {
        return this.getGodkjentAvArbeidsgiver() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvVeileder() {
        return this.getGodkjentAvVeileder() != null;
    }

    @JsonProperty
    public boolean erAvtaleInngått() {
        return this.getAvtaleInngått() != null;
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
        setGodkjentAvNavIdent(null);
        sistEndretNå();
    }

    private void sistEndretNå() {
        this.sistEndret = Instant.now();
    }

    void sjekkSistEndret(Instant sistEndret) {
        if (sistEndret == null || sistEndret.isBefore(this.sistEndret)) {
            throw new SamtidigeEndringerException();
        }
    }

    void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkOmAltErUtfylt();
        this.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        sistEndretNå();
        registerEvent(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    void godkjennForVeileder(NavIdent utfortAv) {
        sjekkOmAltErUtfylt();
        if (erUfordelt()) {
            throw new AvtaleErIkkeFordeltException();
        }
        if (!erGodkjentAvArbeidsgiver() || !erGodkjentAvDeltaker()) {
            throw new VeilederSkalGodkjenneSistException();
        }

        LocalDateTime tidspunkt = LocalDateTime.now();
        this.setGodkjentAvVeileder(tidspunkt);
        this.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        this.setIkrafttredelsestidspunkt(tidspunkt);
        sistEndretNå();
        registerEvent(new GodkjentAvVeileder(this, utfortAv));
    }

    private void avtaleInngått(LocalDateTime tidspunkt, Avtalerolle utførtAvRolle, NavIdent utførtAv) {
        setAvtaleInngått(tidspunkt);
        registerEvent(new AvtaleInngått(this, utførtAvRolle, utførtAv));
    }

    void godkjennForVeilederOgDeltaker(NavIdent utfortAv, GodkjentPaVegneGrunn paVegneAvGrunn) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErUtfylt();
        if (erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (!erGodkjentAvArbeidsgiver()) {
            throw new ArbeidsgiverSkalGodkjenneFørVeilederException();
        }
        paVegneAvGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = LocalDateTime.now();
        this.setGodkjentAvVeileder(tidspunkt);
        this.setGodkjentAvDeltaker(tidspunkt);
        this.setGodkjentPaVegneAv(true);
        this.setGodkjentPaVegneGrunn(paVegneAvGrunn);
        this.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        this.setIkrafttredelsestidspunkt(tidspunkt);
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAv(this, utfortAv));
    }

    void godkjennForVeilederOgArbeidsgiver(NavIdent utfortAv, GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErUtfylt();
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_TILTAKSTYPE);
        }
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.ARBEIDSGIVER_HAR_GODKJENT);
        }
        if (!erGodkjentAvDeltaker()) {
            throw new FeilkodeException(Feilkode.DELTAKER_SKAL_GODKJENNE_FOER_VEILEDER);
        }
        godkjentPaVegneAvArbeidsgiverGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = LocalDateTime.now();
        this.setGodkjentAvVeileder(tidspunkt);
        this.setGodkjentAvArbeidsgiver(tidspunkt);
        this.setGodkjentPaVegneAvArbeidsgiver(true);
        this.setGodkjentPaVegneAvArbeidsgiverGrunn(godkjentPaVegneAvArbeidsgiverGrunn);
        this.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        this.setIkrafttredelsestidspunkt(tidspunkt);
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAvArbeidsgiver(this, utfortAv));
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(NavIdent utfortAv, GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErUtfylt();
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_TILTAKSTYPE);
        }
        if (erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.ARBEIDSGIVER_HAR_GODKJENT);
        }
        paVegneAvDeltakerOgArbeidsgiverGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = LocalDateTime.now();
        this.setGodkjentAvVeileder(tidspunkt);
        this.setGodkjentAvDeltaker(tidspunkt);
        this.setGodkjentAvArbeidsgiver(tidspunkt);
        this.setGodkjentPaVegneAv(true);
        this.setGodkjentPaVegneAvArbeidsgiver(true);
        this.setGodkjentPaVegneGrunn(paVegneAvDeltakerOgArbeidsgiverGrunn.getGodkjentPaVegneAvDeltakerGrunn());
        this.setGodkjentPaVegneAvArbeidsgiverGrunn(paVegneAvDeltakerOgArbeidsgiverGrunn.getGodkjentPaVegneAvArbeidsgiverGrunn());
        this.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        this.setIkrafttredelsestidspunkt(tidspunkt);
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAvDeltakerOgArbeidsgiver(this, utfortAv));
    }

    void godkjennForDeltaker(Identifikator utfortAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErUtfylt();
        this.setGodkjentAvDeltaker(LocalDateTime.now());
        sistEndretNå();
        registerEvent(new GodkjentAvDeltaker(this, utfortAv));
    }

    void sjekkOmAltErUtfylt() {
        if (!erAltUtfylt()) {
            throw new AltMåVæreFyltUtException();
        }
    }

    @JsonProperty
    public String status() {
        return statusSomEnum().getStatusVerdi();
    }

    @JsonProperty
    public Status statusSomEnum() {
        if (getAnnullertTidspunkt() != null) {
            return Status.ANNULLERT;
        } else if (isAvbrutt()) {
            return Status.AVBRUTT;
        } else if (erAvtaleInngått() && (this.getSluttDato().isBefore(LocalDate.now()))) {
            return Status.AVSLUTTET;
        } else if (erAvtaleInngått() && (this.getStartDato().isBefore(LocalDate.now().plusDays(1)))) {
            return Status.GJENNOMFØRES;
        } else if (erAvtaleInngått()) {
            return Status.KLAR_FOR_OPPSTART;
        } else if (erAltUtfylt()) {
            return Status.MANGLER_GODKJENNING;
        } else {
            return Status.PÅBEGYNT;
        }
    }

    @JsonProperty
    public boolean kanAvbrytes() {
        return !isAvbrutt();
    }

    @JsonProperty
    public boolean kanGjenopprettes() {
        return isAvbrutt();
    }

    public void annuller(Veileder veileder, String annullerGrunn) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        annullerTilskuddsperioder();
        setAnnullertTidspunkt(Instant.now());
        setAnnullertGrunn(annullerGrunn);
        if (erUfordelt()) {
            setVeilederNavIdent(veileder.getIdentifikator());
        }
        if ("Feilregistrering".equals(annullerGrunn)) {
            setFeilregistrert(true);
        }
        sistEndretNå();
        registerEvent(new AnnullertAvVeileder(this, veileder.getIdentifikator()));
    }

    // TODO: Skal slettes, ikke i bruk
    public void avbryt(Veileder veileder, AvbruttInfo avbruttInfo) {
        if (this.kanAvbrytes()) {
            this.setAvbrutt(true);
            this.setAvbruttDato(avbruttInfo.getAvbruttDato());
            this.setAvbruttGrunn(avbruttInfo.getAvbruttGrunn());
            if (this.erUfordelt()) {
                this.setVeilederNavIdent(veileder.getIdentifikator());
            }
            sistEndretNå();
            registerEvent(new AvbruttAvVeileder(this, veileder.getIdentifikator()));
        }
    }

    public void overtaAvtale(NavIdent nyNavIdent) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        NavIdent gammelNavIdent = this.getVeilederNavIdent();
        this.setVeilederNavIdent(nyNavIdent);
        sistEndretNå();
        if (gammelNavIdent == null) {
            this.registerEvent(new AvtaleOpprettetAvArbeidsgiverErFordelt(this));
        } else {
            registerEvent(new AvtaleNyVeileder(this, gammelNavIdent));
        }
    }

    // TODO: Skal slettes, ikke i bruk
    public void gjenopprett(Veileder veileder) {
        if (this.kanGjenopprettes()) {
            this.setAvbrutt(false);
            this.setAvbruttDato(null);
            this.setAvbruttGrunn(null);
            sistEndretNå();
            registerEvent(new AvtaleGjenopprettet(this, veileder.getIdentifikator()));
        }
    }

    private boolean erAltUtfylt() {
        return felterSomIkkeErFyltUt().isEmpty();
    }

    public void leggTilBedriftNavn(String bedriftNavn) {
        this.setBedriftNavn(bedriftNavn);
    }

    public void leggTilDeltakerNavn(Navn navn) {
        NavnFormaterer formaterer = new NavnFormaterer(navn);
        this.setDeltakerFornavn(formaterer.getFornavn());
        this.setDeltakerEtternavn(formaterer.getEtternavn());
    }

    @JsonProperty
    public Set<String> felterSomIkkeErFyltUt() {
        return gjeldendeInnhold().felterSomIkkeErFyltUt();
    }

    @JsonProperty
    public boolean kanLåsesOpp() {
        return erGodkjentAvVeileder();
    }

    public void sjekkOmKanLåsesOpp() {
        if (!kanLåsesOpp()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAASES_OPP);
        }
    }

    private void annullerTilskuddsperiode(TilskuddPeriode tilskuddsperiode) {
        tilskuddsperiode.setStatus(TilskuddPeriodeStatus.ANNULLERT);
        registerEvent(new TilskuddsperiodeAnnullert(this, tilskuddsperiode));
    }

    public void låsOppAvtale() {
        sjekkOmKanLåsesOpp();
        tilskuddPeriode.stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.GODKJENT).forEach(this::annullerTilskuddsperiode);
        versjoner.add(this.gjeldendeInnhold().nyVersjon(AvtaleInnholdType.LÅSE_OPP));
        sistEndretNå();
        registerEvent(new AvtaleLåstOpp(this));
    }

    @JsonProperty
    public boolean erUfordelt() {
        return this.getVeilederNavIdent() == null;
    }

    public void godkjennTilskuddsperiode(NavIdent beslutter, String enhet) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_BEHANDLES_VED_INNGAATT_AVTALE);
        }
        if (enhet == null || !enhet.matches("^\\d{4}$")) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER);
        }
        if (beslutter.equals(getGodkjentAvNavIdent())) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE);
        }
        TilskuddPeriode gjeldendePeriode = gjeldendeTilskuddsperiode();
        gjeldendePeriode.godkjenn(beslutter, enhet);
        if (!erAvtaleInngått()) {
            LocalDateTime tidspunkt = LocalDateTime.now();
            godkjennForBeslutter(tidspunkt, beslutter);
            avtaleInngått(tidspunkt, Avtalerolle.BESLUTTER, beslutter);
        }
        sistEndretNå();
        registerEvent(new TilskuddsperiodeGodkjent(this, gjeldendePeriode, beslutter));
    }

    private void godkjennForBeslutter(LocalDateTime tidspunkt, NavIdent beslutter) {
        setGodkjentAvBeslutter(tidspunkt);
        setGodkjentAvBeslutterNavIdent(beslutter);
    }

    public void avslåTilskuddsperiode(NavIdent beslutter, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_BEHANDLES_VED_INNGAATT_AVTALE);
        }
        TilskuddPeriode gjeldendePeriode = gjeldendeTilskuddsperiode();
        gjeldendePeriode.avslå(beslutter, avslagsårsaker, avslagsforklaring);
        sistEndretNå();
        registerEvent(new TilskuddsperiodeAvslått(this, beslutter, gjeldendePeriode));
    }


    protected TilskuddPeriodeStatus getGjeldendeTilskuddsperiodestatus() {
        TilskuddPeriode tilskuddPeriode = gjeldendeTilskuddsperiode();
        if (tilskuddPeriode == null) {
            return null;
        }
        return tilskuddPeriode.getStatus();
    }

    public TilskuddPeriode tilskuddsperiode(int index) {
        return tilskuddPeriode.toArray(new TilskuddPeriode[0])[index];
    }

    @JsonProperty
    public TilskuddPeriode gjeldendeTilskuddsperiode() {
        TreeSet<TilskuddPeriode> aktiveTilskuddsperioder = new TreeSet(tilskuddPeriode.stream().filter(t -> t.isAktiv()).collect(Collectors.toSet()));

        if (aktiveTilskuddsperioder.isEmpty()) {
            return null;
        }

        // Finner første avslått
        Optional<TilskuddPeriode> førsteAvslått = aktiveTilskuddsperioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.AVSLÅTT).findFirst();
        if (førsteAvslått.isPresent()) {
            return førsteAvslått.get();
        }

        // Finn første som kan behandles
        Optional<TilskuddPeriode> førsteSomKanBehandles = aktiveTilskuddsperioder.stream().filter(TilskuddPeriode::kanBehandles).findFirst();
        if (førsteSomKanBehandles.isPresent()) {
            return førsteSomKanBehandles.get();
        }

        // Finn siste godkjent
        Optional<TilskuddPeriode> sisteGodkjent = aktiveTilskuddsperioder.descendingSet().stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.GODKJENT)
                .findFirst();
        if (sisteGodkjent.isPresent()) {
            return sisteGodkjent.get();
        }

        return aktiveTilskuddsperioder.first();
    }

    public TreeSet<TilskuddPeriode> finnTilskuddsperioderIkkeLukketForEndring() {
        TreeSet<TilskuddPeriode> tilskuddsperioder = tilskuddPeriode.stream()
                .filter(t -> t.isAktiv() && (t.getStatus().equals(TilskuddPeriodeStatus.UBEHANDLET) ||
                        t.getStatus().equals(TilskuddPeriodeStatus.AVSLÅTT))).distinct()
                .collect(Collectors.toCollection(TreeSet::new));
        if (tilskuddsperioder.isEmpty()) {
            return null;
        }
        return tilskuddsperioder;
    }

    public void oppdatereKostnadsstedForTilskuddsperioder(NyttKostnadssted nyttKostnadssted) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_OPPDATERE_KOSTNADSSTED_INGAATT_AVTALE);
        }
        setEnhetKostnadssted(nyttKostnadssted.getEnhet());
        setEnhetsnavnKostnadssted(nyttKostnadssted.getEnhetsnavn());
        nyeTilskuddsperioder();
    }

    public void slettemerk(NavIdent utførtAv) {
        this.setSlettemerket(true);
        registerEvent(new AvtaleSlettemerket(this, utførtAv));
    }

    void forlengTilskuddsperioder(LocalDate gammelSluttDato, LocalDate nySluttDato) {
        if (tilskuddPeriode.isEmpty()) {
            return;
        }
        TilskuddPeriode sisteTilskuddsperiode = tilskuddPeriode.last();
        if (sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            // Kan utvide siste tilskuddsperiode hvis den er ubehandlet
            tilskuddPeriode.remove(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = beregnTilskuddsperioder(sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer());
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else {
            // Regner ut nye perioder fra gammel avtaleslutt til ny avtaleslutt
            List<TilskuddPeriode> nyeTilskuddperioder = beregnTilskuddsperioder(gammelSluttDato.plusDays(1), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        }
    }

    private void fikseLøpenumre(List<TilskuddPeriode> tilskuddperioder, int startPåLøpenummer) {
        for (int i = 0; i < tilskuddperioder.size(); i++) {
            tilskuddperioder.get(i).setLøpenummer(startPåLøpenummer + i);
        }
    }

    private void annullerTilskuddsperioder() {
        for (TilskuddPeriode tilskuddsperiode : Set.copyOf(tilskuddPeriode)) {
            TilskuddPeriodeStatus status = tilskuddsperiode.getStatus();
            if (status == TilskuddPeriodeStatus.UBEHANDLET) {
                tilskuddPeriode.remove(tilskuddsperiode);
            } else if (status == TilskuddPeriodeStatus.GODKJENT) {
                annullerTilskuddsperiode(tilskuddsperiode);
            }
        }
    }

    private void forkortTilskuddsperioder(LocalDate nySluttDato) {
        for (TilskuddPeriode tilskuddsperiode : Set.copyOf(tilskuddPeriode)) {
            TilskuddPeriodeStatus status = tilskuddsperiode.getStatus();
            if (tilskuddsperiode.getStartDato().isAfter(nySluttDato)) {
                if (status == TilskuddPeriodeStatus.UBEHANDLET) {
                    tilskuddPeriode.remove(tilskuddsperiode);
                } else if (status == TilskuddPeriodeStatus.GODKJENT) {
                    annullerTilskuddsperiode(tilskuddsperiode);
                }
            } else if (tilskuddsperiode.getSluttDato().isAfter(nySluttDato)) {
                if (status == TilskuddPeriodeStatus.UBEHANDLET || status == TilskuddPeriodeStatus.GODKJENT) {
                    tilskuddsperiode.setSluttDato(nySluttDato);
                    tilskuddsperiode.setBeløp(beregnTilskuddsbeløp(tilskuddsperiode.getStartDato(), tilskuddsperiode.getSluttDato()));
                    if (status == TilskuddPeriodeStatus.GODKJENT) {
                        registerEvent(new TilskuddsperiodeForkortet(this, tilskuddsperiode));
                    }
                }
            }
        }
    }

    void endreBeløpITilskuddsperioder() {
        sendTilbakeTilBeslutter();
        tilskuddPeriode.stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET).forEach(t -> t.setBeløp(beregnTilskuddsbeløp(t.getStartDato(), t.getSluttDato())));
    }

    public void sendTilbakeTilBeslutter() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        var rettede = tilskuddPeriode.stream()
                .filter(t -> t.isAktiv())
                .filter(t -> t.getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
                .map(TilskuddPeriode::deaktiverOgLagNyUbehandlet)
                .collect(Collectors.toList());
        tilskuddPeriode.addAll(rettede);
    }

    private void sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt() {
        if (erAnnullertEllerAvbrutt()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_ANNULLERT_AVTALE);
        }
    }

    @JsonProperty
    public boolean erAnnullertEllerAvbrutt() {
        return isAvbrutt() || annullertTidspunkt != null;
    }

    private Integer beregnTilskuddsbeløp(LocalDate startDato, LocalDate sluttDato) {
        return RegnUtTilskuddsperioderForAvtale.beløpForPeriode(startDato, sluttDato, getDatoForRedusertProsent(), getSumLonnstilskudd(), getSumLønnstilskuddRedusert());
    }

    private List<TilskuddPeriode> beregnTilskuddsperioder(LocalDate startDato, LocalDate sluttDato) {
        List<TilskuddPeriode> tilskuddsperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(getSumLonnstilskudd(), startDato, sluttDato, getLonnstilskuddProsent(), getDatoForRedusertProsent(), getSumLønnstilskuddRedusert());
        tilskuddsperioder.forEach(t -> t.setAvtale(this));
        tilskuddsperioder.forEach(t -> t.setEnhet(getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    private void nyeTilskuddsperioder() {
        tilskuddPeriode.removeIf(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET);
        if (Utils.erIkkeTomme(getStartDato(), getSluttDato(), getSumLonnstilskudd())) {
            List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioder(getStartDato(), getSluttDato());

            fikseLøpenumre(tilskuddsperioder, 1);
            tilskuddPeriode.addAll(tilskuddsperioder);
        }
    }

    public void forkortAvtale(LocalDate nySluttDato, String grunn, String annetGrunn, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isBefore(getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_ETTER_SLUTTDATO);
        }
        sjekkStartOgSluttDato(getStartDato(), nySluttDato);
        if (StringUtils.isBlank(grunn) || (grunn.equals("Annet") && StringUtils.isBlank(annetGrunn))) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_GRUNN_MANGLER);
        }
        AvtaleInnhold nyAvtaleInnholdVersjon = gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORKORTE);
        versjoner.add(nyAvtaleInnholdVersjon);
        gjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
        forkortTilskuddsperioder(nySluttDato);
        registerEvent(new AvtaleForkortet(this, nyAvtaleInnholdVersjon, nySluttDato, grunn, annetGrunn, utførtAv));
    }

    public void forlengAvtale(LocalDate nySluttDato, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isAfter(getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_FEIL_SLUTTDATO);
        }
        sjekkStartOgSluttDato(getStartDato(), nySluttDato);
        var gammelSluttDato = getSluttDato();
        AvtaleInnhold nyVersjon = gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        versjoner.add(nyVersjon);
        gjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
        forlengTilskuddsperioder(gammelSluttDato, nySluttDato);
        sistEndretNå();
        registerEvent(new AvtaleForlenget(this, utførtAv));
    }

    private void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategyFactory.create(getTiltakstype()).sjekkStartOgSluttDato(startDato, sluttDato);
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning tilskuddsberegning, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        krevEnAvTiltakstyper(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB);
        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OKONOMI_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(tilskuddsberegning.getArbeidsgiveravgift(),
                tilskuddsberegning.getFeriepengesats(),
                tilskuddsberegning.getManedslonn(),
                tilskuddsberegning.getOtpSats())) {

            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OKONOMI_UGYLDIG_INPUT);
        }
        versjoner.add(gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_TILSKUDDSBEREGNING));
        gjeldendeInnhold().endreTilskuddsberegning(tilskuddsberegning);
        endreBeløpITilskuddsperioder();
        sistEndretNå();
        gjeldendeInnhold().setIkrafttredelsestidspunkt(LocalDateTime.now());
        registerEvent(new TilskuddsberegningEndret(this, utførtAv));
    }

    private void krevEnAvTiltakstyper(Tiltakstype... tiltakstyper) {
        if (Stream.of(tiltakstyper).noneMatch(t -> t == tiltakstype)) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE);
        }
    }

    public void endreKontaktInformasjon(EndreKontaktInformasjon endreKontaktInformasjon, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KONTAKTINFO_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(endreKontaktInformasjon.getDeltakerFornavn(),
                endreKontaktInformasjon.getDeltakerEtternavn(),
                endreKontaktInformasjon.getDeltakerTlf(), endreKontaktInformasjon.getVeilederFornavn(),
                endreKontaktInformasjon.getVeilederEtternavn(),
                endreKontaktInformasjon.getVeilederTlf(),
                endreKontaktInformasjon.getArbeidsgiverFornavn(),
                endreKontaktInformasjon.getArbeidsgiverEtternavn(),
                endreKontaktInformasjon.getArbeidsgiverTlf())
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KONTAKTINFO_GRUNN_MANGLER);
        }
        versjoner.add(gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_KONTAKTINFO));
        gjeldendeInnhold().endreKontaktInfo(endreKontaktInformasjon);
        gjeldendeInnhold().setIkrafttredelsestidspunkt(LocalDateTime.now());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new KontaktinformasjonEndret(this, utførtAv));
    }

    public void endreStillingsbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_STILLINGSBESKRIVELSE_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(endreStillingsbeskrivelse.getStillingstittel(),
                endreStillingsbeskrivelse.getArbeidsoppgaver(),
                endreStillingsbeskrivelse.getStillingStyrk08(),
                endreStillingsbeskrivelse.getStillingKonseptId(),
                endreStillingsbeskrivelse.getStillingprosent(),
                endreStillingsbeskrivelse.getAntallDagerPerUke())
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_STILLINGSBESKRIVELSE_GRUNN_MANGLER);
        }
        versjoner.add(gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_STILLING));
        gjeldendeInnhold().endreStillingsInfo(endreStillingsbeskrivelse);
        gjeldendeInnhold().setIkrafttredelsestidspunkt(LocalDateTime.now());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new StillingsbeskrivelseEndret(this, utførtAv));
    }

    public void endreOppfølgingOgTilrettelegging(EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OPPFØLGING_OG_TILRETTELEGGING_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(endreOppfølgingOgTilrettelegging.getOppfolging(),
                endreOppfølgingOgTilrettelegging.getTilrettelegging())
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OPPFØLGING_OG_TILRETTELEGGING_GRUNN_MANGLER);
        }
        versjoner.add(gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_OPPFØLGING_OG_TILRETTELEGGING));
        gjeldendeInnhold().endreOppfølgingOgTilretteleggingInfo(endreOppfølgingOgTilrettelegging);
        gjeldendeInnhold().setIkrafttredelsestidspunkt(LocalDateTime.now());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new OppfølgingOgTilretteleggingEndret(this, utførtAv));
    }

    public void endreMål(EndreMål endreMål, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        krevEnAvTiltakstyper(Tiltakstype.ARBEIDSTRENING);
        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_MAAL_IKKE_INNGAATT_AVTALE);
        }
        if (endreMål.getMaal().isEmpty()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_MAAL_TOM_LISTE);
        }
        for (Maal m : endreMål.getMaal()) {
            if (Utils.erNoenTomme(m.getBeskrivelse(), m.getKategori())) {
                throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_MAAL_IKKE_BESKRIVELSE_ELLER_KATEGORI);
            }
        }
        versjoner.add(gjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_MÅL));
        gjeldendeInnhold().getMaal().clear();
        List<Maal> nyeMål = endreMål.getMaal().stream().map(m -> new Maal().setId(UUID.randomUUID()).setBeskrivelse(m.getBeskrivelse()).setKategori(m.getKategori())).collect(Collectors.toList());
        gjeldendeInnhold().getMaal().addAll(nyeMål);
        gjeldendeInnhold().getMaal().forEach(m -> m.setAvtaleInnhold(gjeldendeInnhold()));
        gjeldendeInnhold().setIkrafttredelsestidspunkt(LocalDateTime.now());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new MålEndret(this, utførtAv));
    }

    private interface MetoderSomIkkeSkalDelegeresFraAvtaleInnhold {
        UUID getId();

        void setId(UUID id);

        Avtale getAvtale();

        void endreTilskuddsberegning(EndreTilskuddsberegning tilskuddsberegning);

        Set<String> felterSomIkkeErFyltUt();
    }
}