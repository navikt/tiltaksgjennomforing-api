package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import no.bekk.bekkopen.banking.KidnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.ArbeidsgiversGodkjenningOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleDeltMedAvtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndretAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleFordelt;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleNyVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleUtloperVarsel;
import no.nav.tag.tiltaksgjennomforing.avtale.events.DeltakersGodkjenningOpphevetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.DeltakersGodkjenningOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.FjernetEtterregistrering;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentForEtterregistrering;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltakerOgArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KidOgKontonummerEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfolgingAvAvtaleGodkjent;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonFristForlenget;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlar;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlarRevarsel;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKorrigert;
import no.nav.tag.tiltaksgjennomforing.avtale.events.SignertAvMentor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAnnullert;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAvslått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeForkortet;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeGodkjent;
import no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy.StartOgSluttDatoStrategyFactory;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltMåVæreFyltUtException;
import no.nav.tag.tiltaksgjennomforing.exceptions.ArbeidsgiverSkalGodkjenneFørVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.AvtaleErIkkeFordeltException;
import no.nav.tag.tiltaksgjennomforing.exceptions.DeltakerHarGodkjentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditerbarEntitet;
import no.nav.tag.tiltaksgjennomforing.oppfolging.Oppfolging;
import no.nav.tag.tiltaksgjennomforing.persondata.NavnFormaterer;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.BeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.utils.TelefonnummerValidator;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SortNatural;
import org.hibernate.generator.EventType;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.tag.tiltaksgjennomforing.avtale.ForkortetGrunn.AVSLUTTET_I_ARENA;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.sjekkAtIkkeNull;

@Slf4j
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder
public class Avtale extends AbstractAggregateRoot<Avtale> implements AuditerbarEntitet {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    @Convert(converter = FnrConverter.class)
    private Fnr deltakerFnr;
    @Convert(converter = FnrConverter.class)
    private Fnr mentorFnr;
    @Convert(converter = BedriftNrConverter.class)
    private BedriftNr bedriftNr;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent veilederNavIdent;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private Tiltakstype tiltakstype;

    private Instant opprettetTidspunkt;

    @Generated(event = EventType.INSERT)
    private Integer avtaleNr;

    @OneToOne(cascade = CascadeType.ALL)
    private AvtaleInnhold gjeldendeInnhold;

    private Instant sistEndret;
    private Instant annullertTidspunkt;
    private String annullertGrunn;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;

    @Enumerated(EnumType.STRING)
    private Avtaleopphav opphav;

    /**
     * NB: Ønsker ikke å endre status direkte, kall heller .endreAvtale(),
     * som også utfører nødvendige opprydninger.
     */
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    private Status status = Status.PÅBEGYNT;

    private boolean godkjentForEtterregistrering;

    @Enumerated(EnumType.STRING)
    private Kvalifiseringsgruppe kvalifiseringsgruppe;
    @Enumerated(EnumType.STRING)
    private Formidlingsgruppe formidlingsgruppe;

    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    @JsonIgnore
    private TilskuddPeriode gjeldendeTilskuddsperiode;
    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @SortNatural
    private SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    private boolean feilregistrert;

    @JsonIgnore
    @OneToOne(mappedBy = "avtale", fetch = FetchType.EAGER)
    private ArenaRyddeAvtale arenaRyddeAvtale;

    @JsonIgnore
    @Transient
    private FnrOgBedrift fnrOgBedrift;

    @JsonIgnore
    @Transient
    private AtomicReference<BeregningStrategy> beregningStrategy = new AtomicReference<>();

    private LocalDate kreverOppfolgingFom = null;

    private Instant oppfolgingVarselSendt = null;

    private Avtale(OpprettAvtale opprettAvtale) {
        sjekkAtIkkeNull(opprettAvtale.getDeltakerFnr(), "Deltakers fnr må være satt.");
        sjekkAtIkkeNull(opprettAvtale.getBedriftNr(), "Arbeidsgivers bedriftnr må være satt.");
        if (opprettAvtale.getDeltakerFnr().erUnder16år()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_IKKE_GAMMEL_NOK);
        }
        if (opprettAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && opprettAvtale.getDeltakerFnr()
            .erOver30årFørsteJanuar()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL);
        }

        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = Now.instant();
        this.deltakerFnr = opprettAvtale.getDeltakerFnr();
        this.bedriftNr = opprettAvtale.getBedriftNr();
        this.fnrOgBedrift = new FnrOgBedrift(this.deltakerFnr, this.bedriftNr);
        this.tiltakstype = opprettAvtale.getTiltakstype();
        this.sistEndret = Now.instant();
        this.gjeldendeInnhold = AvtaleInnhold.nyttTomtInnhold(tiltakstype);
        this.gjeldendeInnhold.setAvtale(this);
    }

    private Avtale(OpprettMentorAvtale opprettMentorAvtale) {
        sjekkAtIkkeNull(opprettMentorAvtale.getDeltakerFnr(), "Deltakers fnr må være satt.");
        sjekkAtIkkeNull(opprettMentorAvtale.getBedriftNr(), "Arbeidsgivers bedriftnr må være satt.");
        if (opprettMentorAvtale.getDeltakerFnr().erUnder16år()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_IKKE_GAMMEL_NOK);
        }
        if (opprettMentorAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && opprettMentorAvtale.getDeltakerFnr()
            .erOver30årFørsteJanuar()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL);
        }

        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = Now.instant();
        this.deltakerFnr = opprettMentorAvtale.getDeltakerFnr();
        this.mentorFnr = opprettMentorAvtale.getMentorFnr();
        this.bedriftNr = opprettMentorAvtale.getBedriftNr();
        this.fnrOgBedrift = new FnrOgBedrift(this.deltakerFnr, this.bedriftNr);
        this.tiltakstype = opprettMentorAvtale.getTiltakstype();
        this.sistEndret = Now.instant();
        this.gjeldendeInnhold = AvtaleInnhold.nyttTomtInnhold(tiltakstype);
        this.gjeldendeInnhold.setAvtale(this);
    }

    public static Avtale opprett(OpprettAvtale opprettAvtale, Avtaleopphav opphav) {
        return opprett(opprettAvtale, opphav, null);
    }

    public static Avtale opprett(OpprettAvtale opprettAvtale, Avtaleopphav opphav, NavIdent navIdent) {
        Avtale avtale = (opprettAvtale instanceof OpprettMentorAvtale opprettMentorAvtale)
            ? new Avtale(opprettMentorAvtale)
            : new Avtale(opprettAvtale);

        switch (opphav) {
            case VEILEDER -> {
                avtale.veilederNavIdent = sjekkAtIkkeNull(navIdent, "Veileders NAV-ident må være satt.");
                avtale.registerEvent(new AvtaleOpprettetAvVeileder(avtale, navIdent));
            }
            case ARBEIDSGIVER -> avtale.registerEvent(new AvtaleOpprettetAvArbeidsgiver(avtale));
            case ARENA -> avtale.registerEvent(new AvtaleOpprettetAvArena(avtale));
            default -> throw new IllegalArgumentException(opphav.name() + " kan ikke opprette avtale.");
        }

        avtale.setOpphav(opphav);
        return avtale;
    }

    protected boolean harOppfølgingsStatus() {
        return this.getEnhetOppfolging() != null
            && this.getKvalifiseringsgruppe() != null
            && this.getFormidlingsgruppe() != null;
    }

    public void endreAvtale(
        EndreAvtale nyAvtale,
        Avtalerolle utfortAvRolle,
        Identifikator identifikator
    ) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAvtalenKanEndres();
        sjekkStartOgSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        getGjeldendeInnhold().endreAvtale(nyAvtale);
        nyeTilskuddsperioder();

        settFoersteOppfolgingstidspunkt();
        utforEndring(new AvtaleEndret(this, AvtaleHendelseUtførtAv.Rolle.fra(utfortAvRolle), identifikator));
    }

    public void endreStatus(Status nyStatus) {
        if (nyStatus.erAvsluttetEllerAnnullert() && getKreverOppfolgingFom() != null) {
            setOppfolgingVarselSendt(null);
            setKreverOppfolgingFom(null);
        }
        this.status = nyStatus;
    }

    /**
     * Dersom tiltaket avtalen gjelder for krever oppfølging må vi sørge for at første oppfølging starter på riktig
     * tidspunkt ved endringer i avtalen.
     */
    private void settFoersteOppfolgingstidspunkt() {
        if (Tiltakstype.VTAO.equals(this.getTiltakstype()) && this.gjeldendeInnhold.getStartDato() != null) {
            Oppfolging oppfolging = Oppfolging.fra(this)
                .nullstill()
                .neste();
            setKreverOppfolgingFom(oppfolging.getVarselstidspunkt());
        }
    }

    /**
     * En midlertidig metode for å oppdatere startdatoen til en gammel avtale som har fått feil startdato
     */
    public void midlertidigEndreAvtale(LocalDate nyStartDato) {
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        if (getGjeldendeInnhold().getStartDato().equals(nyStartDato)) {
            return;
        }
        getGjeldendeInnhold().setStartDato(nyStartDato);
        utforEndring(new AvtaleEndret(this, AvtaleHendelseUtførtAv.Rolle.SYSTEM, Identifikator.SYSTEM));
    }

    public void endreAvtale(
        EndreAvtale nyAvtale,
        Avtalerolle utfortAv
    ) {
        endreAvtale(nyAvtale, utfortAv, null);
    }

    public void endreAvtaleArena(EndreAvtaleArena endreAvtaleArena) {
        if (!erGodkjentAvVeileder()) {
            throw new IllegalStateException(
                "Dette skal ikke kunne skje. Avtale fra Arena skal være inngått og godkjent.");
        }

        EndreAvtaleArena.Handling action = endreAvtaleArena.getHandling();
        if (EndreAvtaleArena.Handling.OPPDATER == action && endreAvtaleArena.compareTo(this) == 0) {
            log.atInfo()
                .addKeyValue("avtaleId", getId().toString())
                .log("Endringer fra Arena er lik innholdet i avtalen. Beholder avtalen uendret.");
            registerEvent(new AvtaleEndretAvArena(this));
            return;
        }

        if (EndreAvtaleArena.Handling.ANNULLER == action) {
            annuller(AnnullertGrunn.ANNULLERT_I_ARENA, Identifikator.ARENA);
            return;
        }

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRET_AV_ARENA);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());

        if (EndreAvtaleArena.Handling.AVSLUTT == action) {
            LocalDate sluttDato = Stream.of(endreAvtaleArena.getSluttdato(), gjeldendeInnhold.getSluttDato())
                .filter(dato -> dato.isBefore(LocalDate.now()))
                .findFirst()
                .orElse(LocalDate.now().minusDays(1));

            LocalDate startDato = Stream.of(endreAvtaleArena.getStartdato(), gjeldendeInnhold.getStartDato())
                .filter(dato -> dato.isEqual(sluttDato) || dato.isBefore(sluttDato))
                .findFirst()
                .orElse(LocalDate.now().minusDays(1));

            getGjeldendeInnhold().setStartDato(startDato);
            getGjeldendeInnhold().setSluttDato(sluttDato);

            utforEndring(new AvtaleForkortetAvArena(
                this,
                gjeldendeInnhold,
                sluttDato,
                ForkortetGrunn.av(AVSLUTTET_I_ARENA, null)
            ));
            return;
        }

        boolean isForlengelse = EndreAvtaleArena.Handling.GJENOPPRETT == action ||
            Optional.ofNullable(endreAvtaleArena.getStartdato())
                .map(arenaStartdato -> arenaStartdato.isAfter(gjeldendeInnhold.getSluttDato()))
                .orElse(false);

        Optional.ofNullable(endreAvtaleArena.getStartdato()).ifPresent(getGjeldendeInnhold()::setStartDato);
        Optional.ofNullable(endreAvtaleArena.getSluttdato()).ifPresent(getGjeldendeInnhold()::setSluttDato);
        Optional.ofNullable(endreAvtaleArena.getStillingprosent()).ifPresent(getGjeldendeInnhold()::setStillingprosent);
        Optional.ofNullable(endreAvtaleArena.getAntallDagerPerUke())
            .ifPresent(getGjeldendeInnhold()::setAntallDagerPerUke);

        setAnnullertTidspunkt(null);
        setAnnullertGrunn(null);
        setFeilregistrert(false);
        nyeTilskuddsperioder();

        if (isForlengelse) {
            utforEndring(new AvtaleForlengetAvArena(this));
        } else {
            utforEndring(new AvtaleEndretAvArena(this));
        }
    }

    public void delMedAvtalepart(Avtalerolle avtalerolle) {
        sjekkAtIkkeAvtaleErAnnullert();

        String tlf = telefonnummerTilAvtalepart(avtalerolle);
        if (!TelefonnummerValidator.erGyldigMobilnummer(tlf)) {
            throw new FeilkodeException(Feilkode.UGYLDIG_TLF);
        }
        registerEvent(new AvtaleDeltMedAvtalepart(this, avtalerolle));
    }

    public void refusjonKlar(LocalDate fristForGodkjenning) {
        sjekkAtIkkeAvtaleErAnnullert();
        registerEvent(new RefusjonKlar(this, fristForGodkjenning));
    }

    public void refusjonRevarsel(LocalDate fristForGodkjenning) {
        sjekkAtIkkeAvtaleErAnnullert();
        registerEvent(new RefusjonKlarRevarsel(this, fristForGodkjenning));
    }

    public void refusjonFristForlenget() {
        sjekkAtIkkeAvtaleErAnnullert();
        registerEvent(new RefusjonFristForlenget(this));
    }

    public void refusjonKorrigert() {
        sjekkAtIkkeAvtaleErAnnullert();
        registerEvent(new RefusjonKorrigert(this));
    }

    private String telefonnummerTilAvtalepart(Avtalerolle avtalerolle) {
        return switch (avtalerolle) {
            case DELTAKER -> gjeldendeInnhold.getDeltakerTlf();
            case ARBEIDSGIVER -> gjeldendeInnhold.getArbeidsgiverTlf();
            case VEILEDER -> gjeldendeInnhold.getVeilederTlf();
            case MENTOR -> gjeldendeInnhold.getMentorTlf();
            default -> throw new IllegalArgumentException();
        };
    }

    @JsonProperty
    public boolean erRyddeAvtale() {
        return arenaRyddeAvtale != null;
    }

    @JsonProperty
    public boolean erLaast() {
        return erGodkjentAvVeileder() && erGodkjentAvArbeidsgiver() && erGodkjentAvDeltaker();
    }

    @JsonProperty
    public boolean erGodkjentAvDeltaker() {
        return gjeldendeInnhold.getGodkjentAvDeltaker() != null;
    }

    @JsonProperty
    public boolean erGodkjentTaushetserklæringAvMentor() {
        if (gjeldendeInnhold == null) {
            return false;
        }
        return gjeldendeInnhold.getGodkjentTaushetserklæringAvMentor() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvArbeidsgiver() {
        return gjeldendeInnhold.getGodkjentAvArbeidsgiver() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvVeileder() {
        return gjeldendeInnhold.getGodkjentAvVeileder() != null;
    }

    @JsonProperty
    public boolean erAvtaleInngått() {
        return gjeldendeInnhold.getAvtaleInngått() != null;
    }

    @JsonProperty
    public Instant godkjentAvDeltaker() {
        return gjeldendeInnhold.getGodkjentAvDeltaker();
    }

    @JsonProperty
    public Instant godkjentAvMentor() {
        return gjeldendeInnhold.getGodkjentTaushetserklæringAvMentor();
    }

    @JsonProperty
    public Instant godkjentAvArbeidsgiver() {
        return gjeldendeInnhold.getGodkjentAvArbeidsgiver();
    }

    @JsonProperty
    public Instant godkjentAvVeileder() {
        return gjeldendeInnhold.getGodkjentAvVeileder();
    }

    @JsonProperty
    public Instant godkjentAvBeslutter() {
        return gjeldendeInnhold.getGodkjentAvBeslutter();
    }

    @JsonProperty
    private Instant avtaleInngått() {
        return gjeldendeInnhold.getAvtaleInngått();
    }

    @JsonProperty
    private NavIdent godkjentAvNavIdent() {
        return gjeldendeInnhold.getGodkjentAvNavIdent();
    }

    @JsonProperty
    private NavIdent godkjentAvBeslutterNavIdent() {
        return gjeldendeInnhold.getGodkjentAvBeslutterNavIdent();
    }

    @JsonProperty
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn() {
        return gjeldendeInnhold.getGodkjentPaVegneGrunn();
    }

    @JsonProperty
    private boolean godkjentPaVegneAv() {
        return gjeldendeInnhold.isGodkjentPaVegneAv();
    }

    @JsonProperty
    private GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn() {
        return gjeldendeInnhold.getGodkjentPaVegneAvArbeidsgiverGrunn();
    }

    @JsonProperty
    private boolean godkjentPaVegneAvArbeidsgiver() {
        return gjeldendeInnhold.isGodkjentPaVegneAvArbeidsgiver();
    }

    @JsonProperty
    public LocalDate getKreverOppfolgingFrist() {
        return Oppfolging.fra(this).getOppfolgingsfrist();
    }

    private void sjekkOmAvtalenKanEndres() {
        if (erGodkjentAvDeltaker() || erGodkjentAvArbeidsgiver() || erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.SAMTIDIGE_ENDRINGER);
        }
    }

    void opphevGodkjenningerSomArbeidsgiver() {
        boolean varGodkjentAvDeltaker = erGodkjentAvDeltaker();
        opphevGodkjenninger();
        utforEndring(new GodkjenningerOpphevetAvArbeidsgiver(this, new GamleVerdier(varGodkjentAvDeltaker, false)));
        if (varGodkjentAvDeltaker) {
            utforEndring(new DeltakersGodkjenningOpphevetAvArbeidsgiver(this));
        }
    }

    public void opphevGodkjenningerSomVeileder() {
        boolean varGodkjentAvDeltaker = erGodkjentAvDeltaker();
        boolean varGodkjentAvArbeidsgiver = erGodkjentAvArbeidsgiver();
        opphevGodkjenninger();
        utforEndring(new GodkjenningerOpphevetAvVeileder(
            this,
            new GamleVerdier(varGodkjentAvDeltaker, varGodkjentAvArbeidsgiver)
        ));
        if (varGodkjentAvDeltaker) {
            utforEndring(new DeltakersGodkjenningOpphevetAvVeileder(this));
        }
        if (varGodkjentAvArbeidsgiver) {
            utforEndring(new ArbeidsgiversGodkjenningOpphevetAvVeileder(this));
        }
    }

    private void opphevGodkjenninger() {
        gjeldendeInnhold.setGodkjentAvDeltaker(null);
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(null);
        gjeldendeInnhold.setGodkjentAvVeileder(null);
        gjeldendeInnhold.setGodkjentPaVegneAv(false);
        gjeldendeInnhold.setGodkjentPaVegneGrunn(null);
        gjeldendeInnhold.setGodkjentAvNavIdent(null);
    }

    private void utførEndring() {
        utforEndring(null);
    }

    private <T> void utforEndring(T event) {
        endreStatus(Status.fra(this));
        this.gjeldendeTilskuddsperiode = TilskuddPeriode.finnGjeldende(this);
        this.sistEndret = Now.instant();

        if (event != null) {
            registerEvent(event);
        }
    }

    public void sjekkSistEndret(Instant sistEndret) {
        if (sistEndret == null || sistEndret.isBefore(this.sistEndret)) {
            throw new SamtidigeEndringerException();
        }
    }

    public void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_ARBEIDSGIVER_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(Now.instant());
        utforEndring(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    public void godkjennForVeileder(NavIdent utfortAv) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAltErKlarTilGodkjenning();
        sjekkGjeldendeStartogSluttDato();
        if (erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_VEILEDER_HAR_ALLEREDE_GODKJENT);
        }
        if (erUfordelt()) {
            throw new AvtaleErIkkeFordeltException();
        }
        if (this.getTiltakstype() == Tiltakstype.MENTOR && !erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.MENTOR_MÅ_SIGNERE_TAUSHETSERKLÆRING);
        }
        if (!erGodkjentAvArbeidsgiver() || !erGodkjentAvDeltaker()) {
            throw new VeilederSkalGodkjenneSistException();
        }

        Instant tidspunkt = Now.instant();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        inngåAvtale(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        utforEndring(new GodkjentAvVeileder(this, utfortAv));
    }

    private void inngåAvtale(Instant tidspunkt, Avtalerolle utførtAvRolle, NavIdent utførtAv) {
        if (!utførtAvRolle.erInternBruker()) {
            throw new FeilkodeException(Feilkode.IKKE_TILGANG_TIL_A_INNGAA_AVTALE);
        }
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.AVTALE_ER_ALLEREDE_INNGAATT);
        }
        if (utførtAvRolle.erBeslutter() || !tiltakstype.skalBesluttes() || erAlleTilskuddsperioderBehandletIArena()) {
            gjeldendeInnhold.setAvtaleInngått(tidspunkt);
            settFoersteOppfolgingstidspunkt();
            utforEndring(new AvtaleInngått(this, AvtaleHendelseUtførtAv.Rolle.fra(utførtAvRolle), utførtAv));
        }
    }

    private boolean erAlleTilskuddsperioderBehandletIArena() {
        if (Avtaleopphav.ARENA != opphav) {
            return false;
        }
        if (tilskuddPeriode.isEmpty()) {
            return false;
        }
        return tilskuddPeriode.stream().allMatch(periode ->
            TilskuddPeriodeStatus.BEHANDLET_I_ARENA == periode.getStatus()
        );
    }

    void godkjennForVeilederOgDeltaker(NavIdent utfortAv, GodkjentPaVegneGrunn paVegneAvGrunn) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAltErKlarTilGodkjenning();
        sjekkGjeldendeStartogSluttDato();
        if (erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (!erGodkjentAvArbeidsgiver()) {
            throw new ArbeidsgiverSkalGodkjenneFørVeilederException();
        }
        if (erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_VEILEDER_HAR_ALLEREDE_GODKJENT);
        }

        if (tiltakstype == Tiltakstype.MENTOR && !erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.MENTOR_MÅ_SIGNERE_TAUSHETSERKLÆRING);
        }

        paVegneAvGrunn.valgtMinstEnGrunn();
        Instant tidspunkt = Now.instant();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvDeltaker(tidspunkt);
        gjeldendeInnhold.setGodkjentPaVegneAv(true);
        gjeldendeInnhold.setGodkjentPaVegneGrunn(paVegneAvGrunn);
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        inngåAvtale(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        utforEndring(new GodkjentPaVegneAvDeltaker(this, utfortAv));
    }

    void godkjennForVeilederOgArbeidsgiver(
        NavIdent utfortAv,
        GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn
    ) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAltErKlarTilGodkjenning();
        sjekkGjeldendeStartogSluttDato();
        if (Avtaleopphav.ARENA != opphav) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_OPPHAV);
        }
        if (Tiltakstype.MENTOR == tiltakstype) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_TILTAKSTYPE);
        }
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.ARBEIDSGIVER_HAR_GODKJENT);
        }
        if (!erGodkjentAvDeltaker()) {
            throw new FeilkodeException(Feilkode.DELTAKER_SKAL_GODKJENNE_FOER_VEILEDER);
        }
        if (erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_VEILEDER_HAR_ALLEREDE_GODKJENT);
        }

        godkjentPaVegneAvArbeidsgiverGrunn.valgtMinstEnGrunn();
        Instant tidspunkt = Now.instant();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(tidspunkt);
        gjeldendeInnhold.setGodkjentPaVegneAvArbeidsgiver(true);
        gjeldendeInnhold.setGodkjentPaVegneAvArbeidsgiverGrunn(godkjentPaVegneAvArbeidsgiverGrunn);
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        inngåAvtale(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        utforEndring(new GodkjentPaVegneAvArbeidsgiver(this, utfortAv));
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(
        NavIdent utfortAv,
        GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn
    ) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkOmAltErKlarTilGodkjenning();
        sjekkGjeldendeStartogSluttDato();
        if (Avtaleopphav.ARENA != opphav) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_OPPHAV);
        }
        if (Tiltakstype.MENTOR == tiltakstype) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_TILTAKSTYPE);
        }
        if (erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.ARBEIDSGIVER_HAR_GODKJENT);
        }
        if (erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_VEILEDER_HAR_ALLEREDE_GODKJENT);
        }

        paVegneAvDeltakerOgArbeidsgiverGrunn.valgtMinstEnGrunn();
        Instant tidspunkt = Now.instant();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvDeltaker(tidspunkt);
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(tidspunkt);
        gjeldendeInnhold.setGodkjentPaVegneAv(true);
        gjeldendeInnhold.setGodkjentPaVegneAvArbeidsgiver(true);
        gjeldendeInnhold.setGodkjentPaVegneGrunn(paVegneAvDeltakerOgArbeidsgiverGrunn.getGodkjentPaVegneAvDeltakerGrunn());
        gjeldendeInnhold.setGodkjentPaVegneAvArbeidsgiverGrunn(paVegneAvDeltakerOgArbeidsgiverGrunn.getGodkjentPaVegneAvArbeidsgiverGrunn());
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        inngåAvtale(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        utforEndring(new GodkjentPaVegneAvDeltakerOgArbeidsgiver(this, utfortAv));
    }

    public void godkjennForDeltaker(Identifikator utfortAv) {
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvDeltaker()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_DELTAKER_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentAvDeltaker(Now.instant());
        utforEndring(new GodkjentAvDeltaker(this, utfortAv));
    }

    void godkjennForMentor(Identifikator utfortAv) {
        if (erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_MENTOR_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentTaushetserklæringAvMentor(Now.instant());
        utforEndring(new SignertAvMentor(this, utfortAv));
    }

    void sjekkOmAltErKlarTilGodkjenning() {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!felterSomIkkeErFyltUt().isEmpty()) {
            log.warn(
                "Avtale= {}, med type= {} har ikke alle felter fylt ut for godkjenning= {}",
                this.avtaleNr,
                this.tiltakstype,
                felterSomIkkeErFyltUt()
            );
            throw new AltMåVæreFyltUtException();
        }
        if (List.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB)
            .contains(tiltakstype) &&
            Utils.erNoenTomme(
                gjeldendeInnhold.getSumLonnstilskudd(),
                gjeldendeInnhold.getLonnstilskuddProsent(),
                tilskuddPeriode
            )) {
            throw new FeilkodeException(Feilkode.MANGLER_BEREGNING);
        }
        if (veilederNavIdent == null) {
            throw new FeilkodeException(Feilkode.MANGLER_VEILEDER_PÅ_AVTALE);
        }
    }

    public void annuller(Veileder veileder, String annullerGrunn) {
        annuller(annullerGrunn, veileder.getNavIdent());
    }

    public void annuller(String annullerGrunn, Identifikator identifikator) {
        sjekkAtIkkeAvtalenInneholderUtbetaltTilskuddsperiode();
        sjekkAtIkkeAvtaleErAnnullert();

        annullerTilskuddsperioder();
        setAnnullertTidspunkt(Now.instant());
        setAnnullertGrunn(annullerGrunn);
        setFeilregistrert(AnnullertGrunn.skalFeilregistreres(annullerGrunn));

        Optional<NavIdent> veilederNavIdentOpt = Optional.ofNullable(identifikator)
            .filter(i -> i instanceof NavIdent)
            .map(i -> (NavIdent) i);

        if (veilederNavIdentOpt.isEmpty()) {
            utforEndring(new AnnullertAvSystem(this, identifikator));
            return;
        }

        NavIdent veilederNavIdent = veilederNavIdentOpt.get();
        if (erUfordelt()) {
            setVeilederNavIdent(veilederNavIdent);
        }

        utforEndring(new AnnullertAvVeileder(this, veilederNavIdent));
    }

    public void utlop(AvtaleUtlopHandling handling) {
        switch (handling) {
            case VARSEL_EN_UKE -> registerEvent(new AvtaleUtloperVarsel(this, AvtaleUtloperVarsel.Type.OM_EN_UKE));
            case VARSEL_24_TIMER -> registerEvent(new AvtaleUtloperVarsel(this, AvtaleUtloperVarsel.Type.OM_24_TIMER));
            case UTLOP -> annuller(AnnullertGrunn.UTLØPT, Identifikator.SYSTEM);
        }
    }

    private void sjekkAtIkkeAvtalenInneholderUtbetaltTilskuddsperiode() {
        if (this.getTilskuddPeriode().stream().anyMatch(TilskuddPeriode::erUtbetalt)) {
            throw new FeilkodeException(Feilkode.AVTALE_INNEHOLDER_UTBETALT_TILSKUDDSPERIODE);
        }
        if (this.getTilskuddPeriode().stream().anyMatch(TilskuddPeriode::erRefusjonGodkjent)) {
            throw new FeilkodeException(Feilkode.AVTALE_INNEHOLDER_TILSKUDDSPERIODE_MED_GODKJENT_REFUSJON);
        }
    }

    public void overtaAvtale(NavIdent nyNavIdent) {
        sjekkAtIkkeAvtaleErAnnullert();
        NavIdent gammelNavIdent = this.getVeilederNavIdent();
        this.setVeilederNavIdent(nyNavIdent);
        getGjeldendeInnhold().reberegnLønnstilskudd();
        if (gammelNavIdent == null) {
            nyeTilskuddsperioder();
            utforEndring(new AvtaleFordelt(this));
        } else {
            utforEndring(new AvtaleNyVeileder(this, gammelNavIdent));
        }
    }

    public void leggTilBedriftNavn(String bedriftNavn) {
        gjeldendeInnhold.setBedriftNavn(bedriftNavn);
    }

    public void leggTilDeltakerNavn(Navn navn) {
        NavnFormaterer formaterer = new NavnFormaterer(navn);
        gjeldendeInnhold.setDeltakerFornavn(formaterer.getFornavn());
        gjeldendeInnhold.setDeltakerEtternavn(formaterer.getEtternavn());
    }

    public void leggTilMentorNavn(Navn navn) {
        NavnFormaterer formaterer = new NavnFormaterer(navn);
        gjeldendeInnhold.setMentorFornavn(formaterer.getFornavn());
        gjeldendeInnhold.setMentorEtternavn(formaterer.getEtternavn());
    }

    @JsonProperty
    public Set<String> felterSomIkkeErFyltUt() {
        return getGjeldendeInnhold().felterSomIkkeErFyltUt();
    }

    public void annullerTilskuddsperiode(TilskuddPeriode tilskuddsperiode) {
        // Sjekk på refusjonens status
        if (tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.UTGÅTT) {
            log.atWarn()
                .addKeyValue("avtaleId", getId().toString())
                .log(
                    "Sender ikke annuleringsmelding for tilskuddsperiode {} med utgått refusjon.",
                    tilskuddsperiode.getId()
                );
        } else {
            tilskuddsperiode.setStatus(TilskuddPeriodeStatus.ANNULLERT);
            registerEvent(new TilskuddsperiodeAnnullert(this, tilskuddsperiode));
        }
    }

    @JsonProperty
    public boolean erUfordelt() {
        return this.getVeilederNavIdent() == null;
    }

    public void godkjennTilskuddsperiode(NavIdent beslutter, String enhet) {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_BEHANDLES_VED_INNGAATT_AVTALE);
        }
        if (enhet == null || !enhet.matches("^\\d{4}$")) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER);
        }
        if (beslutter.equals(gjeldendeInnhold.getGodkjentAvNavIdent())) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE);
        }
        TilskuddPeriode gjeldendePeriode = getGjeldendeTilskuddsperiode();

        // Sjekk om samme løpenummer allerede er godkjent og annullert. Trenger da en "ekstra" resendingsnummer
        Integer resendingsnummer = finnResendingsNummer(gjeldendePeriode);
        gjeldendePeriode.godkjenn(beslutter, enhet);
        if (!erAvtaleInngått()) {
            Instant tidspunkt = Now.instant();
            godkjennForBeslutter(tidspunkt, beslutter);
            inngåAvtale(tidspunkt, Avtalerolle.BESLUTTER, beslutter);
        }
        utforEndring(new TilskuddsperiodeGodkjent(this, gjeldendePeriode, beslutter, resendingsnummer));
    }

    private void godkjennForBeslutter(Instant tidspunkt, NavIdent beslutter) {
        gjeldendeInnhold.setGodkjentAvBeslutter(tidspunkt);
        gjeldendeInnhold.setGodkjentAvBeslutterNavIdent(beslutter);
    }

    public void avslåTilskuddsperiode(
        NavIdent beslutter,
        EnumSet<Avslagsårsak> avslagsårsaker,
        String avslagsforklaring
    ) {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_BEHANDLES_VED_INNGAATT_AVTALE);
        }
        TilskuddPeriode gjeldendePeriode = getGjeldendeTilskuddsperiode();
        gjeldendePeriode.avslå(beslutter, avslagsårsaker, avslagsforklaring);
        utforEndring(new TilskuddsperiodeAvslått(this, beslutter, gjeldendePeriode));
    }

    public void togglegodkjennEtterregistrering(NavIdent beslutter) {
        sjekkAtIkkeAvtaleErAnnullert();
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_MERKES_FOR_ETTERREGISTRERING_AVTALE_GODKJENT);
        }
        setGodkjentForEtterregistrering(!this.godkjentForEtterregistrering);
        if (this.godkjentForEtterregistrering) {
            utforEndring(new GodkjentForEtterregistrering(this, beslutter));
        } else {
            utforEndring(new FjernetEtterregistrering(this, beslutter));
        }
    }

    protected TilskuddPeriodeStatus getGjeldendeTilskuddsperiodestatus() {
        TilskuddPeriode tilskuddPeriode = getGjeldendeTilskuddsperiode();
        if (tilskuddPeriode == null) {
            return null;
        }
        return tilskuddPeriode.getStatus();
    }

    public TilskuddPeriode tilskuddsperiode(int index) {
        return tilskuddPeriode.toArray(new TilskuddPeriode[0])[index];
    }


    @Nullable
    @JsonProperty
    public TilskuddPeriode getGjeldendeTilskuddsperiode() {
        return getGjeldendeTilskuddsperiode(true);
    }

    public void setGjeldendeTilskuddsperiode(TilskuddPeriode tilskuddPeriode) {
        log.atInfo()
            .addKeyValue("avtaleId", this.getId().toString())
            .log(
                "Oppdaterer tilskuddsperiode til {} (løpenr {}, status {})",
                tilskuddPeriode != null ? tilskuddPeriode.getId() : null,
                tilskuddPeriode != null ? tilskuddPeriode.getLøpenummer() : null,
                tilskuddPeriode != null ? tilskuddPeriode.getStatus() : null
            );
        this.gjeldendeTilskuddsperiode = tilskuddPeriode;
    }

    /**
     * Vi ønsker å migrere til at "gjeldende tilskuddsperiode" lagres i databasen for å legge til rette for bedre
     * filtreringsmuligheter for besluttere. I en overgangsfase bør all logikk basere seg på gammel implementasjon som
     * "kalkulerer" gjeldende periode, men vi bør også logge eventuelle avvik for å sikre at systemet fungerer likt som
     * før etter endringen.
     * <p>
     * TODO: Fjern gammel logikk, og denne disclaimeren
     */
    public TilskuddPeriode getGjeldendeTilskuddsperiode(boolean kalkulerNyTilskuddsperiode) {
        var gjeldendeFraDb = this.gjeldendeTilskuddsperiode;
        if (!kalkulerNyTilskuddsperiode) {
            return gjeldendeFraDb;
        }
        var gjeldendePeriode = TilskuddPeriode.finnGjeldende(this);
        var erLike = Objects.equals(gjeldendePeriode, gjeldendeFraDb);
        if (!erLike) {
            log.atWarn()
                .addKeyValue("avtaleId", this.getId().toString())
                .log(
                    "Gjeldende tilskuddsperiode ikke oppdatert på avtale {} med status {}? Fant {} {} {}, men kalkulerte {} {} {}",
                    id,
                    status,
                    Optional.ofNullable(gjeldendeFraDb).map(TilskuddPeriode::getId).orElse(null),
                    Optional.ofNullable(gjeldendeFraDb).map(TilskuddPeriode::getLøpenummer).orElse(null),
                    Optional.ofNullable(gjeldendeFraDb).map(TilskuddPeriode::getStatus).orElse(null),
                    Optional.ofNullable(gjeldendePeriode).map(TilskuddPeriode::getId).orElse(null),
                    Optional.ofNullable(gjeldendePeriode).map(TilskuddPeriode::getLøpenummer).orElse(null),
                    Optional.ofNullable(gjeldendePeriode).map(TilskuddPeriode::getStatus).orElse(null)
                );
        }
        return gjeldendePeriode;
    }

    public TreeSet<TilskuddPeriode> finnTilskuddsperioderIkkeLukketForEndring() {
        TreeSet<TilskuddPeriode> tilskuddsperioder = tilskuddPeriode.stream()
            .filter(t -> t.isAktiv() && (t.getStatus().equals(TilskuddPeriodeStatus.UBEHANDLET) ||
                t.getStatus().equals(TilskuddPeriodeStatus.AVSLÅTT)))
            .collect(Collectors.toCollection(TreeSet::new));
        if (tilskuddsperioder.isEmpty()) {
            return null;
        }
        return tilskuddsperioder;
    }

    public void oppdatereKostnadsstedForTilskuddsperioder(NyttKostnadssted nyttKostnadssted) {
        sjekkAtIkkeAvtaleErAnnullert();
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_OPPDATERE_KOSTNADSSTED_INGAATT_AVTALE);
        }
        gjeldendeInnhold.setEnhetKostnadssted(nyttKostnadssted.getEnhet());
        gjeldendeInnhold.setEnhetsnavnKostnadssted(nyttKostnadssted.getEnhetsnavn());
        nyeTilskuddsperioder();
        utførEndring();
    }

    void forlengTilskuddsperioder(LocalDate gammelSluttDato, LocalDate nySluttDato) {
        hentBeregningStrategi().forleng(this, gammelSluttDato, nySluttDato);
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
                    tilskuddsperiode.setBeløp(beregnTilskuddsbeløpForPeriode(
                        tilskuddsperiode.getStartDato(),
                        tilskuddsperiode.getSluttDato()
                    ));
                    if (status == TilskuddPeriodeStatus.GODKJENT) {
                        registerEvent(new TilskuddsperiodeForkortet(this, tilskuddsperiode));
                    }
                }
            }
        }
    }

    void endreBeløpOgProsentITilskuddsperioder() {
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        tilskuddPeriode.stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET)
            .forEach(t -> {
                t.setBeløp(beregnTilskuddsbeløpForPeriode(t.getStartDato(), t.getSluttDato()));
                t.setLonnstilskuddProsent(beregnTilskuddsprosentForPeriode(t.getSluttDato()));
            });
    }

    void reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter() {
        sjekkAtIkkeAvtaleErAnnullert();
        var rettede = tilskuddPeriode.stream()
            .filter(TilskuddPeriode::isAktiv)
            .filter(t -> t.getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
            .map(TilskuddPeriode::deaktiverOgLagNyUbehandlet).toList();
        tilskuddPeriode.addAll(rettede);
        utførEndring();
    }

    private void sjekkAtIkkeAvtaleErAnnullert() {
        if (Status.ANNULLERT.equals(status)) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_ANNULLERT_AVTALE);
        }
    }

    protected Integer beregnTilskuddsprosentForPeriode(LocalDate sluttDato) {
        return BeregningStrategy.tilskuddprosentForPeriode(
            sluttDato,
            tiltakstype,
            gjeldendeInnhold.getDatoForRedusertProsent(),
            gjeldendeInnhold.getLonnstilskuddProsent()
        );
    }

    protected Integer beregnTilskuddsbeløpForPeriode(LocalDate startDato, LocalDate sluttDato) {
        return this.hentBeregningStrategi().beregnTilskuddsbeløpForPeriode(this, startDato, sluttDato);
    }

    private void nyeTilskuddsperioder() {
        List<TilskuddPeriode> nyeTilskuddsperioder = this.hentBeregningStrategi().genererNyeTilskuddsperioder(this);
        boolean harNyeTilskuddsperioder = !(tilskuddPeriode.equals(new TreeSet<>(nyeTilskuddsperioder)));
        if (harNyeTilskuddsperioder) {
            tilskuddPeriode.clear();
            tilskuddPeriode.addAll(nyeTilskuddsperioder);
        }
    }

    private boolean sjekkRyddingAvTilskuddsperioder() {
        if (!this.hentBeregningStrategi().nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(this)) {
            // TODO: Her blir det trøbbel i migrering pga start og sluttdato. her må vi refaktorere litt!!
            return false;
        }
        // Statuser som skal få tilskuddsperioder
        return status != Status.ANNULLERT;
    }

    /**
     * Avtaler (lønnstilskudd) som avsluttes i Arena må få tilskuddsperioder her.
     * <p>
     * - Sjekk at avtalen ikke allerede har perioder (altså en pilotavtale)
     * - Tilskuddsperioder lages fra startdato til sluttdato, de som er før dato for migrering settes til en ny status, f eks. BEHANDLET_I_ARENA
     * - Sjekk logikk som skjer ved godkjenning av første perioden
     * - Tar ikke høyde for perioder med lengde tre måneder som i arena
     */
    public boolean nyeTilskuddsperioderEtterMigreringFraArena(LocalDate migreringsDato) {
        if (sjekkRyddingAvTilskuddsperioder()) {
            for (TilskuddPeriode tilskuddsperiode : Set.copyOf(tilskuddPeriode)) {
                TilskuddPeriodeStatus status = tilskuddsperiode.getStatus();
                if (status == TilskuddPeriodeStatus.UBEHANDLET || status == TilskuddPeriodeStatus.BEHANDLET_I_ARENA) {
                    tilskuddPeriode.remove(tilskuddsperiode);
                } else if (status == TilskuddPeriodeStatus.GODKJENT) {

                    if (tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.SENDT_KRAV || tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.UTBETALT) {
                        log.atError()
                            .addKeyValue("avtaleId", getId().toString())
                            .log(
                                "Prøver å rydde tilskuddsperiode {} som har status: {}",
                                tilskuddsperiode.getId(),
                                tilskuddsperiode.getRefusjonStatus()
                            );
                    } else {
                        annullerTilskuddsperiode(tilskuddsperiode);
                    }

                } else {
                    log.atError()
                        .addKeyValue("avtaleId", getId().toString())
                        .log(
                            "Prøver rydde tilskuddsperioder for en avtale, men statusen er ikke UBEHANDLET, eller GODKJENT (som blir annullert) på periode {}",
                            tilskuddsperiode.getId()
                        );
                }
            }

            List<TilskuddPeriode> tilskuddsperioder = this.hentBeregningStrategi().hentTilskuddsperioderForPeriode(this, gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato()); //.genererNyeTilskuddsperioder(this);

            tilskuddsperioder.forEach(periode -> {
                // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                if (periode.getSluttDato().minusDays(1).isBefore(migreringsDato)) {
                    periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                }
            });
            fikseLøpenumre(tilskuddsperioder, 1);
            tilskuddPeriode.addAll(tilskuddsperioder);
            setGjeldendeTilskuddsperiode(TilskuddPeriode.finnGjeldende(this));
            settFoersteOppfolgingstidspunkt();
            return true;
        } else {
            log.atInfo()
                .addKeyValue("avtaleId", getId().toString())
                .log(
                    "Avtale {} har allerede tilskuddsperioder eller en status som ikke skal ha perioder, eller er ikke tilstrekkelig fylt ut, genererer ikke nye",
                    id
                );
            return false;
        }
    }

    public void lagNyGodkjentTilskuddsperiodeFraAnnullertPeriode(TilskuddPeriode annullertTilskuddPeriode) {
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB,
            Tiltakstype.VTAO,
            Tiltakstype.MENTOR
        );
        if (annullertTilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        TilskuddPeriode nyTilskuddsperiode = annullertTilskuddPeriode.deaktiverOgLagNyUbehandlet();
        annullertTilskuddPeriode.setAktiv(true);
        nyTilskuddsperiode.setStatus(TilskuddPeriodeStatus.GODKJENT);
        nyTilskuddsperiode.setGodkjentAvNavIdent(annullertTilskuddPeriode.getGodkjentAvNavIdent());
        nyTilskuddsperiode.setGodkjentTidspunkt(annullertTilskuddPeriode.getGodkjentTidspunkt());
        nyTilskuddsperiode.setEnhet(annullertTilskuddPeriode.getEnhet());
        Integer resendingsnummer = finnResendingsNummer(annullertTilskuddPeriode);
        registerEvent(new TilskuddsperiodeGodkjent(
            this,
            nyTilskuddsperiode,
            nyTilskuddsperiode.getGodkjentAvNavIdent(),
            resendingsnummer
        ));
        tilskuddPeriode.add(nyTilskuddsperiode);
        setGjeldendeTilskuddsperiode(TilskuddPeriode.finnGjeldende(this));
    }

    private Integer finnResendingsNummer(TilskuddPeriode gjeldendePeriode) {
        Integer resendingsnummer = null;
        for (TilskuddPeriode periode : tilskuddPeriode) {
            if (periode.getStatus() == TilskuddPeriodeStatus.ANNULLERT && periode.getLøpenummer()
                .equals(gjeldendePeriode.getLøpenummer())) {
                if (resendingsnummer == null) {
                    resendingsnummer = 0;
                }
                resendingsnummer++;
            }
        }
        return resendingsnummer;
    }

    public void lagNyTilskuddsperiodeFraAnnullertPeriode(TilskuddPeriode annullertTilskuddPeriode) {
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
        );
        if (annullertTilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        TilskuddPeriode nyUbehandletPeriode = annullertTilskuddPeriode.deaktiverOgLagNyUbehandlet();
        annullertTilskuddPeriode.setAktiv(true);
        tilskuddPeriode.add(nyUbehandletPeriode);
    }

    public void lagNyBehandletIArenaTilskuddsperiodeFraAnnullertPeriode(TilskuddPeriode annullertTilskuddPeriode) {
        krevEnAvTiltakstyper(
            Tiltakstype.VTAO,
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
        );
        if (annullertTilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_ALLEREDE_BEHANDLET);
        }
        TilskuddPeriode nyUbehandletPeriode = annullertTilskuddPeriode.deaktiverOgLagNyUbehandlet();
        nyUbehandletPeriode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        annullertTilskuddPeriode.setAktiv(true);
        tilskuddPeriode.add(nyUbehandletPeriode);
    }

    public void forkortAvtale(LocalDate nySluttDato, ForkortetGrunn forkortetGrunn, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), nySluttDato);

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isBefore(gjeldendeInnhold.getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_ETTER_SLUTTDATO);
        }
        // Kan ikke forkorte før en utbetalt/sendtkrav tilskuddsperiode
        TreeSet<TilskuddPeriode> aktiveTilskuddsperioder = new TreeSet(tilskuddPeriode.stream()
            .filter(TilskuddPeriode::isAktiv)
            .collect(Collectors.toSet()));
        Optional<TilskuddPeriode> sisteUtbetalt = aktiveTilskuddsperioder.descendingSet()
            .stream()
            .filter(tilskuddPeriode -> (
                tilskuddPeriode.getRefusjonStatus() == RefusjonStatus.SENDT_KRAV ||
                    tilskuddPeriode.getRefusjonStatus() == RefusjonStatus.UTBETALT ||
                    tilskuddPeriode.getRefusjonStatus() == RefusjonStatus.UTBETALING_FEILET ||
                    tilskuddPeriode.getRefusjonStatus() == RefusjonStatus.GODKJENT_MINUSBELØP ||
                    tilskuddPeriode.getRefusjonStatus() == RefusjonStatus.GODKJENT_NULLBELØP)
            )
            .max(Comparator.comparing(TilskuddPeriode::getStartDato));
        if (sisteUtbetalt.isPresent() && nySluttDato.isBefore(sisteUtbetalt.get().getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_FOR_UTBETALT_TILSKUDDSPERIODE);
        }

        if (forkortetGrunn.mangler()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_GRUNN_MANGLER);
        }
        AvtaleInnhold nyAvtaleInnholdVersjon = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORKORTE);
        gjeldendeInnhold = nyAvtaleInnholdVersjon;
        getGjeldendeInnhold().endreSluttDato(nySluttDato);
        LocalDate kreverOppfolgingFrist = getKreverOppfolgingFrist();
        if (kreverOppfolgingFrist != null && kreverOppfolgingFrist.isAfter(nySluttDato)) {
            setKreverOppfolgingFom(null);
        }
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        forkortTilskuddsperioder(nySluttDato);
        utforEndring(new AvtaleForkortetAvVeileder(
            this,
            nyAvtaleInnholdVersjon,
            nySluttDato,
            forkortetGrunn,
            utførtAv
        ));
    }

    public void forlengAvtale(LocalDate nySluttDato, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), nySluttDato);

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isAfter(gjeldendeInnhold.getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_FEIL_SLUTTDATO);
        }

        var gammelSluttDato = gjeldendeInnhold.getSluttDato();
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        getGjeldendeInnhold().endreSluttDato(nySluttDato);

        // Forlenging vil "nullstille" oppfølging til å være 6 mnd fra dagens dato.
        settFoersteOppfolgingstidspunkt();

        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        forlengTilskuddsperioder(gammelSluttDato, nySluttDato);
        utforEndring(new AvtaleForlengetAvVeileder(this, utførtAv));
    }

    private void sjekkGjeldendeStartogSluttDato() {
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
    }

    private void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategyFactory.create(getTiltakstype(), getKvalifiseringsgruppe())
            .sjekkStartOgSluttDato(
                startDato,
                sluttDato,
                isGodkjentForEtterregistrering(),
                erAvtaleInngått(),
                deltakerFnr
            );
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB,
            Tiltakstype.MENTOR
        );
        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OKONOMI_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(
            endreTilskuddsberegning.getArbeidsgiveravgift(),
            endreTilskuddsberegning.getFeriepengesats(),
            endreTilskuddsberegning.getManedslonn(),
            endreTilskuddsberegning.getOtpSats()
        )) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OKONOMI_UGYLDIG_INPUT);
        }
        if (Tiltakstype.VARIG_LONNSTILSKUDD.equals(tiltakstype) && Utils.erTom(endreTilskuddsberegning.getLonnstilskuddProsent())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OKONOMI_UGYLDIG_INPUT);
        }

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_TILSKUDDSBEREGNING);
        this.hentBeregningStrategi().endreBeregning(this, endreTilskuddsberegning);
        endreBeløpOgProsentITilskuddsperioder();
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        utforEndring(new TilskuddsberegningEndret(this, utførtAv));
    }

    // Metode for å rydde opp i beregnede felter som ikke har blitt satt etter at lønnstilskuddsprosent manuelt i databasen har blitt satt inn
    public void reberegnLønnstilskudd() {
        sjekkAtIkkeAvtaleErAnnullert();
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB,
            Tiltakstype.MENTOR
        );
        if (gjeldendeInnhold.getSumLonnstilskudd() == null && Utils.erIkkeTomme(
            gjeldendeInnhold.getLonnstilskuddProsent(),
            gjeldendeInnhold.getArbeidsgiveravgift(),
            gjeldendeInnhold.getFeriepengesats(),
            gjeldendeInnhold.getManedslonn(),
            gjeldendeInnhold.getOtpSats()
        )) {
            getGjeldendeInnhold().reberegnLønnstilskudd();
            return;
        }
        throw new FeilkodeException(Feilkode.KAN_IKKE_REBEREGNE);
    }

    public void reUtregnRedusert() {
        sjekkAtIkkeAvtaleErAnnullert();
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
        );
        if (Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato(),
            gjeldendeInnhold.getSumLonnstilskudd(),
            gjeldendeInnhold.getLonnstilskuddProsent(),
            gjeldendeInnhold.getArbeidsgiveravgift(),
            gjeldendeInnhold.getFeriepengesats(),
            gjeldendeInnhold.getManedslonn(),
            gjeldendeInnhold.getOtpSats()
        )) {

            getGjeldendeInnhold().reberegnRedusertProsentOgRedusertLonnstilskudd();
            return;
        }
        throw new FeilkodeException(Feilkode.KAN_IKKE_REBEREGNE);
    }

    private void krevEnAvTiltakstyper(Tiltakstype... tiltakstyper) {
        if (Stream.of(tiltakstyper).noneMatch(t -> t == tiltakstype)) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_FEIL_TILTAKSTYPE);
        }
    }

    public void endreKontaktInformasjon(EndreKontaktInformasjon endreKontaktInformasjon, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KONTAKTINFO_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(
            endreKontaktInformasjon.getDeltakerFornavn(),
            endreKontaktInformasjon.getDeltakerEtternavn(),
            endreKontaktInformasjon.getDeltakerTlf(), endreKontaktInformasjon.getVeilederFornavn(),
            endreKontaktInformasjon.getVeilederEtternavn(),
            endreKontaktInformasjon.getVeilederTlf(),
            endreKontaktInformasjon.getArbeidsgiverFornavn(),
            endreKontaktInformasjon.getArbeidsgiverEtternavn(),
            endreKontaktInformasjon.getArbeidsgiverTlf()
        )
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KONTAKTINFO_GRUNN_MANGLER);
        }

        if (endreKontaktInformasjon.getRefusjonKontaktperson() != null && !endreKontaktInformasjon.getRefusjonKontaktperson()
            .erTom()) {
            if (Utils.erNoenTomme(
                endreKontaktInformasjon.getRefusjonKontaktperson().getRefusjonKontaktpersonFornavn(),
                endreKontaktInformasjon.getRefusjonKontaktperson().getRefusjonKontaktpersonEtternavn(),
                endreKontaktInformasjon.getRefusjonKontaktperson().getRefusjonKontaktpersonTlf()
            )) {
                throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KONTAKTINFO_GRUNN_MANGLER);
            }
        }

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_KONTAKTINFO);
        getGjeldendeInnhold().endreKontaktInfo(endreKontaktInformasjon);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new KontaktinformasjonEndret(this, utførtAv));
    }

    public void godkjennOppfolgingAvAvtale(NavIdent utførtAv) {
        var nyOppfolgingsdato = Oppfolging.fra(this)
            .neste()
            .getVarselstidspunkt();

        if (nyOppfolgingsdato != null) {
            setOppfolgingVarselSendt(null);
        }
        setKreverOppfolgingFom(nyOppfolgingsdato);

        utforEndring(new OppfolgingAvAvtaleGodkjent(this, utførtAv));

    }

    public void endreStillingsbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_STILLINGSBESKRIVELSE_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(
            endreStillingsbeskrivelse.getStillingstittel(),
            endreStillingsbeskrivelse.getArbeidsoppgaver(),
            endreStillingsbeskrivelse.getStillingStyrk08(),
            endreStillingsbeskrivelse.getStillingKonseptId(),
            endreStillingsbeskrivelse.getStillingprosent(),
            endreStillingsbeskrivelse.getAntallDagerPerUke()
        )
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_STILLINGSBESKRIVELSE_GRUNN_MANGLER);
        }
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_STILLING);
        getGjeldendeInnhold().endreStillingsInfo(endreStillingsbeskrivelse);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        getGjeldendeInnhold().reberegnLønnstilskudd();
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new StillingsbeskrivelseEndret(this, utførtAv));
    }

    public void endreOppfølgingOgTilrettelegging(
        EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging,
        NavIdent utførtAv
    ) {
        sjekkAtIkkeAvtaleErAnnullert();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OPPFØLGING_OG_TILRETTELEGGING_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(
            endreOppfølgingOgTilrettelegging.getOppfolging(),
            endreOppfølgingOgTilrettelegging.getTilrettelegging()
        )
        ) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OPPFØLGING_OG_TILRETTELEGGING_GRUNN_MANGLER);
        }
        gjeldendeInnhold = gjeldendeInnhold.nyGodkjentVersjon(AvtaleInnholdType.ENDRE_OPPFØLGING_OG_TILRETTELEGGING);
        gjeldendeInnhold.endreOppfølgingOgTilretteleggingInfo(endreOppfølgingOgTilrettelegging);
        gjeldendeInnhold.setIkrafttredelsestidspunkt(Now.instant());
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new OppfølgingOgTilretteleggingEndret(this, utførtAv));
    }

    public void endreMål(EndreMål endreMål, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

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
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_MÅL);
        getGjeldendeInnhold().getMaal().clear();
        List<Maal> nyeMål = endreMål.getMaal().stream()
            .map(m -> new Maal()
                .setId(UUID.randomUUID())
                .setBeskrivelse(m.getBeskrivelse())
                .setKategori(m.getKategori()))
            .toList();
        getGjeldendeInnhold().getMaal().addAll(nyeMål);
        getGjeldendeInnhold().getMaal().forEach(m -> m.setAvtaleInnhold(getGjeldendeInnhold()));
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new MålEndret(this, utførtAv));
    }

    public void endreInkluderingstilskudd(EndreInkluderingstilskudd endreInkluderingstilskudd, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

        krevEnAvTiltakstyper(Tiltakstype.INKLUDERINGSTILSKUDD);
        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_IKKE_INNGAATT_AVTALE);
        }
        if (endreInkluderingstilskudd.getInkluderingstilskuddsutgift().isEmpty()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_TOM_LISTE);
        }
        if (endreInkluderingstilskudd.inkluderingstilskuddTotalBeløp() > 143900) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_SUM_FOR_HØY);
        }
        for (Inkluderingstilskuddsutgift i : endreInkluderingstilskudd.getInkluderingstilskuddsutgift()) {
            if (Utils.erNoenTomme(i.getBeløp(), i.getType())) {
                throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_IKKE_BELOP_ELLER_TYPE);
            }
        }
        List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgifterPåForrigeVersjon = getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        List<Inkluderingstilskuddsutgift> forrigeVersjonFraKlient = endreInkluderingstilskudd.getInkluderingstilskuddsutgift()
            .stream()
            .filter(e -> e.getId() != null)
            .toList();

        // Sjekk at det er like mange utgifter på forrige versjon som det er id'er i request. Hvis ikke er ikke frontend i sync
        if (inkluderingstilskuddsutgifterPåForrigeVersjon.size() != forrigeVersjonFraKlient.size()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_TOM_LISTE);
        }
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_INKLUDERINGSTILSKUDD);

        List<Inkluderingstilskuddsutgift> nyeInkluderingstilskuddsutgifter = endreInkluderingstilskudd.getInkluderingstilskuddsutgift()
            .stream()
            .filter(e -> e.getId() == null)
            .map(Inkluderingstilskuddsutgift::new)
            .toList();

        getGjeldendeInnhold().getInkluderingstilskuddsutgift().addAll(nyeInkluderingstilskuddsutgifter);
        getGjeldendeInnhold().getInkluderingstilskuddsutgift().forEach(i -> i.setAvtaleInnhold(getGjeldendeInnhold()));
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new InkluderingstilskuddEndret(this, utførtAv));
    }

    public void endreOmMentor(EndreOmMentor endreOmMentor, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();
        krevEnAvTiltakstyper(Tiltakstype.MENTOR);

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_IKKE_INNGAATT_AVTALE);
        }
        if (Utils.erNoenTomme(
            endreOmMentor.getMentorFornavn(), endreOmMentor.getMentorEtternavn(),
            endreOmMentor.getMentorTlf(), endreOmMentor.getMentorTimelonn(),
            endreOmMentor.getMentorAntallTimer(), endreOmMentor.getMentorOppgaver()
        )) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_UGYLDIG_INPUT);
        }
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_OM_MENTOR);
        getGjeldendeInnhold().endreOmMentor(endreOmMentor);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
        utforEndring(new OmMentorEndret(this, utførtAv));
    }

    public void endreKidOgKontonummer(EndreKidOgKontonummer endreKidOgKontonummer, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullert();

        var kid = endreKidOgKontonummer.getArbeidsgiverKid();
        var kontonummer = endreKidOgKontonummer.getArbeidsgiverKontonummer();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KID_OG_KONTONUMMER_GRUNN_IKKE_GODKJENT_AVTALE);
        }
        if (Utils.erNoenTomme(kontonummer)) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_KID_OG_KONTONUMMER_GRUNN_MANGLER);
        }
        if (kid != null && !KidnummerValidator.isValid(kid)) {
            throw new FeilkodeException(Feilkode.FEIL_KID_NUMMER);
        }

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_KID_OG_KONTONUMMER);
        getGjeldendeInnhold().setArbeidsgiverKid(kid);
        getGjeldendeInnhold().setArbeidsgiverKontonummer(kontonummer);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.instant());
        utforEndring(new KidOgKontonummerEndret(this, utførtAv));
    }

    /**
     * For mentorer vil deltakers fnr skjules; som fører til at auditlogging ikke fungerer
     * med mindre vi oppretter FnrOgBedrift-objektet umiddelbart etter last
     * (og ikke serialiserer det nye feltet på vei ut)
     */
    @PostLoad
    public void opprettFnrOgBedriftEtterDbLast() {
        this.fnrOgBedrift = new FnrOgBedrift(this.deltakerFnr, this.bedriftNr);
    }

    @Override
    public FnrOgBedrift getFnrOgBedrift() {
        return this.fnrOgBedrift;
    }

    protected BeregningStrategy hentBeregningStrategi() {
        return this.beregningStrategy.updateAndGet(strategy -> strategy == null ? BeregningStrategy.create(
            tiltakstype) : strategy);
    }

    public boolean harSluttdatoPassertMedMerEnn12Uker() {
        return this.erGodkjentAvVeileder() && this.getGjeldendeInnhold()
            .getSluttDato().plusWeeks(12)
            .isBefore(Now.localDate());
    }

    public boolean erAnnullertForMerEnn12UkerSiden() {
        return this.getAnnullertTidspunkt() != null && this.getAnnullertTidspunkt()
            .plus(84, ChronoUnit.DAYS)
            .isBefore(Now.instant());
    }

    @JsonProperty
    public boolean erOpprettetEllerEndretAvArena() {
        return getGjeldendeInnhold().getInnholdType() == AvtaleInnholdType.ENDRET_AV_ARENA
            || getOpphav() == Avtaleopphav.ARENA;
    }
}
