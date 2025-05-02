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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.ArbeidsgiversGodkjenningOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleDeltMedAvtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndretAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleNyVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiverErFordelt;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleSlettemerket;
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
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
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
import no.nav.tag.tiltaksgjennomforing.persondata.NavnFormaterer;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.LonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.TilskuddsperioderBeregningStrategyFactory;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
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
import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.maksDato;
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

    private LocalDateTime opprettetTidspunkt;

    @Generated(event = EventType.INSERT)
    private Integer avtaleNr;

    @OneToOne(cascade = CascadeType.ALL)
    private AvtaleInnhold gjeldendeInnhold;

    private Instant sistEndret;
    private Instant annullertTidspunkt;
    private String annullertGrunn;
    private boolean avbrutt;
    private boolean slettemerket;
    private LocalDate avbruttDato;
    private String avbruttGrunn;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;

    @Enumerated(EnumType.STRING)
    private Avtaleopphav opphav;

    @Enumerated(EnumType.STRING)
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
    private AtomicReference<LonnstilskuddAvtaleBeregningStrategy> lonnstilskuddAvtaleBeregningStrategy = new AtomicReference<>();

    private LocalDate kreverOppfolgingFom = null;

    private Instant oppfolgingVarselSendt = null;

    public void leggtilNyeTilskuddsperioder(List<TilskuddPeriode> tilskuddsperioder) {
        this.tilskuddPeriode.addAll(tilskuddsperioder);
    }

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
        this.opprettetTidspunkt = Now.localDateTime();
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
        this.opprettetTidspunkt = Now.localDateTime();
        this.deltakerFnr = opprettMentorAvtale.getDeltakerFnr();
        this.mentorFnr = opprettMentorAvtale.getMentorFnr();
        this.bedriftNr = opprettMentorAvtale.getBedriftNr();
        this.fnrOgBedrift = new FnrOgBedrift(this.deltakerFnr, this.bedriftNr);
        this.tiltakstype = opprettMentorAvtale.getTiltakstype();
        this.sistEndret = Now.instant();
        this.gjeldendeInnhold = AvtaleInnhold.nyttTomtInnhold(tiltakstype);
        this.gjeldendeInnhold.setAvtale(this);
    }

    protected boolean harOppfølgingsStatus() {
        return (this.getEnhetOppfolging() != null ||
            this.getKvalifiseringsgruppe() != null ||
            this.getFormidlingsgruppe() != null);
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

    public void endreAvtale(
        Instant sistEndret,
        EndreAvtale nyAvtale,
        Avtalerolle utfortAvRolle,
        Identifikator identifikator
    ) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAvtalenKanEndres();
        sjekkSistEndret(sistEndret);
        sjekkStartOgSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        getGjeldendeInnhold().endreAvtale(nyAvtale);
        nyeTilskuddsperioder();
        oppdaterKreverOppfolgingFom();
        utforEndring(new AvtaleEndret(this, AvtaleHendelseUtførtAvRolle.fraAvtalerolle(utfortAvRolle), identifikator));
    }

    private void oppdaterKreverOppfolgingFom() {
        if (Tiltakstype.VTAO.equals(this.getTiltakstype()) && this.gjeldendeInnhold.getStartDato() != null) {
            LocalDate tidligstMuligeDato = maksDato(this.gjeldendeInnhold.getStartDato(), Now.localDate());
            LocalDate sluttenAvMnd4MndFremITid = YearMonth.from(tidligstMuligeDato).plusMonths(4).atDay(1);
            this.setKreverOppfolgingFom(sluttenAvMnd4MndFremITid);
        }
    }

    /**
     * En midlertidig metode for å oppdatere startdatoen til en gammel avtale som har fått feil startdato
     */
    public void midlertidigEndreAvtale(
        Instant sistEndret,
        LocalDate nyStartDato
    ) {
        sjekkSistEndret(sistEndret);
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        if (getGjeldendeInnhold().getStartDato().equals(nyStartDato)) {
            return;
        }
        getGjeldendeInnhold().setStartDato(nyStartDato);
        utforEndring(new AvtaleEndret(this, AvtaleHendelseUtførtAvRolle.SYSTEM, Identifikator.SYSTEM));
    }

    public void endreAvtale(
        Instant sistEndret,
        EndreAvtale nyAvtale,
        Avtalerolle utfortAv
    ) {
        endreAvtale(sistEndret, nyAvtale, utfortAv, null);
    }

    public void endreAvtaleArena(EndreAvtaleArena endreAvtaleArena) {
        if (!erGodkjentAvVeileder()) {
            throw new IllegalStateException(
                "Dette skal ikke kunne skje. Avtale fra Arena skal være inngått og godkjent.");
        }

        EndreAvtaleArena.Handling action = endreAvtaleArena.getHandling();
        if (EndreAvtaleArena.Handling.OPPDATER == action && endreAvtaleArena.compareTo(this) == 0) {
            log.info("Endringer fra Arena er lik innholdet i avtalen. Beholder avtalen uendret.");
            registerEvent(new AvtaleEndretAvArena(this));
            return;
        }

        if (EndreAvtaleArena.Handling.ANNULLER == action) {
            annuller(AnnullertGrunn.ANNULLERT_I_ARENA, Identifikator.ARENA);
            return;
        }

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRET_AV_ARENA);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());

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
        setAvbrutt(false);
        setAvbruttDato(null);
        setAvbruttGrunn(null);
        setFeilregistrert(false);
        nyeTilskuddsperioder();

        if (isForlengelse) {
            utforEndring(new AvtaleForlengetAvArena(this));
        } else {
            utforEndring(new AvtaleEndretAvArena(this));
        }
    }

    public void delMedAvtalepart(Avtalerolle avtalerolle) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        String tlf = telefonnummerTilAvtalepart(avtalerolle);
        if (!TelefonnummerValidator.erGyldigMobilnummer(tlf)) {
            throw new FeilkodeException(Feilkode.UGYLDIG_TLF);
        }
        registerEvent(new AvtaleDeltMedAvtalepart(this, avtalerolle));
    }

    public void refusjonKlar(LocalDate fristForGodkjenning) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        registerEvent(new RefusjonKlar(this, fristForGodkjenning));
    }

    public void refusjonRevarsel(LocalDate fristForGodkjenning) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        registerEvent(new RefusjonKlarRevarsel(this, fristForGodkjenning));
    }

    public void refusjonFristForlenget() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        registerEvent(new RefusjonFristForlenget(this));
    }

    public void refusjonKorrigert() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
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
    public LocalDateTime godkjentAvDeltaker() {
        return gjeldendeInnhold.getGodkjentAvDeltaker();
    }

    @JsonProperty
    public LocalDateTime godkjentAvMentor() {
        return gjeldendeInnhold.getGodkjentTaushetserklæringAvMentor();
    }

    @JsonProperty
    public LocalDateTime godkjentAvArbeidsgiver() {
        return gjeldendeInnhold.getGodkjentAvArbeidsgiver();
    }

    @JsonProperty
    public LocalDateTime godkjentAvVeileder() {
        return gjeldendeInnhold.getGodkjentAvVeileder();
    }

    @JsonProperty
    public LocalDateTime godkjentAvBeslutter() {
        return gjeldendeInnhold.getGodkjentAvBeslutter();
    }

    @JsonProperty
    private LocalDateTime avtaleInngått() {
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
        return this.kreverOppfolgingFom == null ? null : YearMonth.from(this.kreverOppfolgingFom)
            .plusMonths(1)
            .atEndOfMonth();
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

    void opphevGodkjenningerSomVeileder() {
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

    private <T> void utforEndring(T event) {
        this.status = Status.fra(this);
        this.sistEndret = Now.instant();

        if (event != null) {
            registerEvent(event);
        }
    }

    void sjekkSistEndret(Instant sistEndret) {
        if (sistEndret == null || sistEndret.isBefore(this.sistEndret)) {
            throw new SamtidigeEndringerException();
        }
    }

    public void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_ARBEIDSGIVER_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(Now.localDateTime());
        utforEndring(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    public void godkjennForVeileder(NavIdent utfortAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
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
        if (this.getTiltakstype() == Tiltakstype.SOMMERJOBB &&
            this.getDeltakerFnr().erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
        } else if (this.getTiltakstype() != Tiltakstype.SOMMERJOBB && this.getDeltakerFnr()
            .erOver72ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (this.getTiltakstype() == Tiltakstype.VTAO && this.getDeltakerFnr()
            .erOver67ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_67_AAR);
        }

        LocalDateTime tidspunkt = Now.localDateTime();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        inngåAvtale(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        utforEndring(new GodkjentAvVeileder(this, utfortAv));
    }

    private void inngåAvtale(LocalDateTime tidspunkt, Avtalerolle utførtAvRolle, NavIdent utførtAv) {
        if (!utførtAvRolle.erInternBruker()) {
            throw new FeilkodeException(Feilkode.IKKE_TILGANG_TIL_A_INNGAA_AVTALE);
        }
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.AVTALE_ER_ALLEREDE_INNGAATT);
        }
        if (utførtAvRolle.erBeslutter() || !tiltakstype.skalBesluttes() || erAlleTilskuddsperioderBehandletIArena()) {
            gjeldendeInnhold.setAvtaleInngått(tidspunkt);
            oppdaterKreverOppfolgingFom();
            utforEndring(new AvtaleInngått(this, AvtaleHendelseUtførtAvRolle.fraAvtalerolle(utførtAvRolle), utførtAv));
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (!erGodkjentAvArbeidsgiver()) {
            throw new ArbeidsgiverSkalGodkjenneFørVeilederException();
        }
        if (erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_VEILEDER_HAR_ALLEREDE_GODKJENT);
        }
        if (this.getTiltakstype() == Tiltakstype.SOMMERJOBB &&
            this.getDeltakerFnr().erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        if (this.getTiltakstype() == Tiltakstype.MENTOR && !erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.MENTOR_MÅ_SIGNERE_TAUSHETSERKLÆRING);
        } else if (this.getTiltakstype() != Tiltakstype.SOMMERJOBB && this.getDeltakerFnr()
            .erOver72ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        }
        if (this.getTiltakstype() == Tiltakstype.VTAO && this.getDeltakerFnr()
            .erOver67ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_67_AAR);
        }

        paVegneAvGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = Now.localDateTime();
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (Avtaleopphav.ARENA != opphav) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_OPPHAV);
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
        if (tiltakstype == Tiltakstype.SOMMERJOBB && this.getDeltakerFnr()
            .erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }
        godkjentPaVegneAvArbeidsgiverGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = Now.localDateTime();
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (Avtaleopphav.ARENA != opphav) {
            throw new FeilkodeException(Feilkode.GODKJENN_PAA_VEGNE_AV_FEIL_OPPHAV);
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
        if (tiltakstype.isSommerjobb() && this.getDeltakerFnr()
            .erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL_FRA_OPPSTARTDATO);
        }

        paVegneAvDeltakerOgArbeidsgiverGrunn.valgtMinstEnGrunn();
        LocalDateTime tidspunkt = Now.localDateTime();
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
        gjeldendeInnhold.setGodkjentAvDeltaker(Now.localDateTime());
        utforEndring(new GodkjentAvDeltaker(this, utfortAv));
    }

    void godkjennForMentor(Identifikator utfortAv) {
        if (erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_MENTOR_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentTaushetserklæringAvMentor(Now.localDateTime());
        utforEndring(new SignertAvMentor(this, utfortAv));
    }

    void sjekkOmAltErKlarTilGodkjenning() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!felterSomIkkeErFyltUt().isEmpty()) {
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

    @JsonProperty
    public boolean kanAvbrytes() {
        return !isAvbrutt();
    }

    @JsonProperty
    public boolean kanGjenopprettes() {
        return isAvbrutt();
    }

    public void annuller(Veileder veileder, String annullerGrunn) {
        annuller(annullerGrunn, veileder.getNavIdent());
    }

    public void annuller(String annullerGrunn, Identifikator identifikator) {
        sjekkAtIkkeAvtalenInneholderUtbetaltTilskuddsperiode();
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        NavIdent gammelNavIdent = this.getVeilederNavIdent();
        this.setVeilederNavIdent(nyNavIdent);
        getGjeldendeInnhold().reberegnLønnstilskudd();
        if (gammelNavIdent == null) {
            nyeTilskuddsperioder();
            utforEndring(new AvtaleOpprettetAvArbeidsgiverErFordelt(this));
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

    @JsonProperty
    public Set<String> felterSomIkkeErFyltUt() {
        return getGjeldendeInnhold().felterSomIkkeErFyltUt();
    }

    public void annullerTilskuddsperiode(TilskuddPeriode tilskuddsperiode) {
        // Sjekk på refusjonens status
        if (tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.UTGÅTT) {
            log.warn(
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
        if (!erAvtaleInngått()) {
            LocalDateTime tidspunkt = Now.localDateTime();
            godkjennForBeslutter(tidspunkt, beslutter);
            inngåAvtale(tidspunkt, Avtalerolle.BESLUTTER, beslutter);
        }
        utforEndring(new TilskuddsperiodeGodkjent(this, gjeldendePeriode, beslutter, resendingsnummer));
    }

    private void godkjennForBeslutter(LocalDateTime tidspunkt, NavIdent beslutter) {
        gjeldendeInnhold.setGodkjentAvBeslutter(tidspunkt);
        gjeldendeInnhold.setGodkjentAvBeslutterNavIdent(beslutter);
    }

    public void avslåTilskuddsperiode(
        NavIdent beslutter,
        EnumSet<Avslagsårsak> avslagsårsaker,
        String avslagsforklaring
    ) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_KAN_KUN_BEHANDLES_VED_INNGAATT_AVTALE);
        }
        TilskuddPeriode gjeldendePeriode = getGjeldendeTilskuddsperiode();
        gjeldendePeriode.avslå(beslutter, avslagsårsaker, avslagsforklaring);
        utforEndring(new TilskuddsperiodeAvslått(this, beslutter, gjeldendePeriode));
    }

    public void togglegodkjennEtterregistrering(NavIdent beslutter) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
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

    /**
     * Vi ønsker å migrere til at "gjeldende tilskuddsperiode" lagres i databasen for å legge til rette for bedre
     * filtreringsmuligheter for besluttere. I en overgangsfase bør all logikk basere seg på gammel implementasjon som
     * "kalkulerer" gjeldende periode, men vi bør også logge eventuelle avvik for å sikre at systemet fungerer likt som
     * før etter endringen.
     * <p>
     * TODO: Fjern gammel logikk, og denne disclaimeren
     */
    @Nullable
    @JsonProperty
    public TilskuddPeriode getGjeldendeTilskuddsperiode() {
        var gjeldendePeriode = finnGjeldendeTilskuddsperiode();
        var gjeldendePeriodeKalkulertId = gjeldendePeriode != null ? gjeldendePeriode.getId() : null;
        var gjeldendeFraDb = this.gjeldendeTilskuddsperiode;
        var gjeldendeFraDbId = gjeldendeFraDb != null ? gjeldendeFraDb.getId() : null;
        if (!Objects.equals(gjeldendePeriodeKalkulertId, gjeldendeFraDbId)) {
            log.warn(
                "Gjeldende tilskuddsperiode ikke oppdatert på avtale {}? Fant {} {} {}, men kalkulerte {} {} {}",
                id,
                gjeldendeFraDbId,
                gjeldendeFraDb != null ? gjeldendeFraDb.getLøpenummer() : null,
                gjeldendeFraDb != null ? gjeldendeFraDb.getStatus() : null,
                gjeldendePeriodeKalkulertId,
                gjeldendePeriode != null ? gjeldendePeriode.getLøpenummer() : null,
                gjeldendePeriode != null ? gjeldendePeriode.getStatus() : null
            );
        }
        return gjeldendePeriode;
    }

    public TilskuddPeriode finnGjeldendeTilskuddsperiode() {
        TreeSet<TilskuddPeriode> aktiveTilskuddsperioder = tilskuddPeriode.stream()
            .filter(TilskuddPeriode::isAktiv)
            .collect(Collectors.toCollection(TreeSet::new));

        if (aktiveTilskuddsperioder.isEmpty()) {
            return null;
        }

        // Finner første avslått
        Optional<TilskuddPeriode> førsteAvslått = aktiveTilskuddsperioder.stream()
            .filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
            .findFirst();
        if (førsteAvslått.isPresent()) {
            return førsteAvslått.get();
        }

        // Finn første som kan behandles
        Optional<TilskuddPeriode> førsteSomKanBehandles = aktiveTilskuddsperioder.stream()
            .filter(TilskuddPeriode::kanBehandles)
            .findFirst();
        if (førsteSomKanBehandles.isPresent()) {
            return førsteSomKanBehandles.get();
        }

        // Finn siste godkjent
        return aktiveTilskuddsperioder.descendingSet().stream()
            .filter(tilskuddPeriode -> tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.GODKJENT)
            .findFirst()
            .orElseGet(aktiveTilskuddsperioder::first);
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_OPPDATERE_KOSTNADSSTED_INGAATT_AVTALE);
        }
        gjeldendeInnhold.setEnhetKostnadssted(nyttKostnadssted.getEnhet());
        gjeldendeInnhold.setEnhetsnavnKostnadssted(nyttKostnadssted.getEnhetsnavn());
        nyeTilskuddsperioder();
    }

    public void slettemerk(NavIdent utførtAv) {
        this.setSlettemerket(true);
        registerEvent(new AvtaleSlettemerket(this, utførtAv));
    }

    void forlengTilskuddsperioder(LocalDate gammelSluttDato, LocalDate nySluttDato) {
        hentBeregningStrategi().forleng(this, gammelSluttDato, nySluttDato);
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
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
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
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
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
    }

    void endreBeløpITilskuddsperioder() {
        sendTilbakeTilBeslutter();
        tilskuddPeriode.stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET)
            .forEach(t -> t.setBeløp(beregnTilskuddsbeløpForPeriode(t.getStartDato(), t.getSluttDato())));
    }

    public void sendTilbakeTilBeslutter() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        var rettede = tilskuddPeriode.stream()
            .filter(TilskuddPeriode::isAktiv)
            .filter(t -> t.getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
            .map(TilskuddPeriode::deaktiverOgLagNyUbehandlet).toList();
        tilskuddPeriode.addAll(rettede);
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
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

    protected Integer beregnTilskuddsbeløpForPeriode(LocalDate startDato, LocalDate sluttDato) {
        return this.hentBeregningStrategi().beregnTilskuddsbeløpForPeriode(this, startDato, sluttDato);
    }

    private List<TilskuddPeriode> beregnTilskuddsperioder(LocalDate startDato, LocalDate sluttDato) {
        return this.hentBeregningStrategi().hentTilskuddsperioderForPeriode(this, startDato, sluttDato);
    }

    private void nyeTilskuddsperioder() {
        this.hentBeregningStrategi().genererNyeTilskuddsperioder(this);
        setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
    }

    private boolean sjekkRyddingAvTilskuddsperioder() {
        if (!this.hentBeregningStrategi().nødvendigeFelterErUtfylt(this)) {
            return false;
        }
        // Statuser som skal få tilskuddsperioder
        return status != Status.ANNULLERT && status != Status.AVBRUTT;
    }

    /**
     * Avtaler (lønnstilskudd) som avsluttes i Arena må få tilskuddsperioder her.
     * <p>
     * - Sjekk at avtalen ikke allerede har perioder (altså en pilotavtale)
     * - Tilskuddsperioder lages fra startdato til sluttdato, de som er før dato for migrering settes til en ny status, f eks. BEHANDLET_I_ARENA
     * - Sjekk logikk som skjer ved godkjenning av første perioden
     * - Tar ikke høyde for perioder med lengde tre måneder som i arena
     */
    public boolean nyeTilskuddsperioderEtterMigreringFraArena(LocalDate migreringsDato, boolean dryRun) {
        if (sjekkRyddingAvTilskuddsperioder()) {

            for (TilskuddPeriode tilskuddsperiode : Set.copyOf(tilskuddPeriode)) {
                TilskuddPeriodeStatus status = tilskuddsperiode.getStatus();
                if (status == TilskuddPeriodeStatus.UBEHANDLET || status == TilskuddPeriodeStatus.BEHANDLET_I_ARENA) {
                    tilskuddPeriode.remove(tilskuddsperiode);
                } else if (status == TilskuddPeriodeStatus.GODKJENT) {

                    if (tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.SENDT_KRAV || tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.UTBETALT) {
                        log.error(
                            "Prøver å rydde tilskuddsperiode {} som har status: {}",
                            tilskuddsperiode.getId(),
                            tilskuddsperiode.getRefusjonStatus()
                        );
                    } else {
                        annullerTilskuddsperiode(tilskuddsperiode);
                    }

                } else {
                    log.error(
                        "Prøver rydde tilskuddsperioder for en avtale, men statusen er ikke UBEHANDLET, eller GODKJENT (som blir annullert) på periode {}",
                        tilskuddsperiode.getId()
                    );
                }
            }

            List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioder(
                gjeldendeInnhold.getStartDato(),
                gjeldendeInnhold.getSluttDato()
            );
            tilskuddsperioder.forEach(periode -> {
                // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                if (periode.getSluttDato().minusDays(1).isBefore(migreringsDato)) {
                    periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                }
            });
            fikseLøpenumre(tilskuddsperioder, 1);
            if (!dryRun) {
                tilskuddPeriode.addAll(tilskuddsperioder);
                setGjeldendeTilskuddsperiode(finnGjeldendeTilskuddsperiode());
                oppdaterKreverOppfolgingFom();
            }
            return true;
        } else {
            log.info(
                "Avtale {} har allerede tilskuddsperioder eller en status som ikke skal ha perioder, eller er ikke tilstrekkelig fylt ut, genererer ikke nye",
                id
            );
            return false;
        }
    }

    public void reberegnUbehandledeTilskuddsperioder() {
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
        );

        // Finn første ubehandlede periode
        //Optional<TilskuddPeriode> førsteUbehandledeTilskuddsperiode = tilskuddPeriode.stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET).findFirst();
        // Fjern ubehandlede
        for (TilskuddPeriode tilskuddsperiode : Set.copyOf(tilskuddPeriode)) {
            TilskuddPeriodeStatus status = tilskuddsperiode.getStatus();
            if (status == TilskuddPeriodeStatus.UBEHANDLET) {
                tilskuddPeriode.remove(tilskuddsperiode);
            }
        }

        // Lag nye fra og med siste ikke ubehandlet + en dag
        LocalDate startDato;
        List<TilskuddPeriode> godkjentePerioder = tilskuddPeriode.stream()
            .filter(t -> t.getStatus() == TilskuddPeriodeStatus.GODKJENT)
            .sorted(Comparator.comparing(TilskuddPeriode::getLøpenummer))
            .toList();

        if (!godkjentePerioder.isEmpty()) {
            startDato = godkjentePerioder.getLast().getSluttDato().plusDays(1);
        } else {
            startDato = tilskuddPeriode.stream().findFirst().map(TilskuddPeriode::getStartDato).orElse(null);
        }

        List<TilskuddPeriode> nyetilskuddsperioder = beregnTilskuddsperioder(
            startDato,
            gjeldendeInnhold.getSluttDato()
        );
        tilskuddPeriode.addAll(nyetilskuddsperioder);
        fikseLøpenumre(tilskuddPeriode.stream().toList(), 1);
    }

    public void lagNyGodkjentTilskuddsperiodeFraAnnullertPeriode(TilskuddPeriode annullertTilskuddPeriode) {
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), nySluttDato);

        if (forkortetGrunn.mangler()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_GRUNN_MANGLER);
        }
        AvtaleInnhold nyAvtaleInnholdVersjon = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORKORTE);
        gjeldendeInnhold = nyAvtaleInnholdVersjon;
        getGjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isAfter(gjeldendeInnhold.getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORLENGE_FEIL_SLUTTDATO);
        }
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), nySluttDato);
        var gammelSluttDato = gjeldendeInnhold.getSluttDato();
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        getGjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
        forlengTilskuddsperioder(gammelSluttDato, nySluttDato);
        utforEndring(new AvtaleForlengetAvVeileder(this, utførtAv));
    }

    private void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategyFactory.create(getTiltakstype(), getKvalifiseringsgruppe())
            .sjekkStartOgSluttDato(startDato, sluttDato, isGodkjentForEtterregistrering(), erAvtaleInngått());
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
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
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_TILSKUDDSBEREGNING);
        this.hentBeregningStrategi().endreBeregning(this, endreTilskuddsberegning);
        endreBeløpITilskuddsperioder();
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        utforEndring(new TilskuddsberegningEndret(this, utførtAv));
    }

    // Metode for å rydde opp i beregnede felter som ikke har blitt satt etter at lønnstilskuddsprosent manuelt i databasen har blitt satt inn
    public void reberegnLønnstilskudd() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        krevEnAvTiltakstyper(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
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
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sendTilbakeTilBeslutter();
        utforEndring(new KontaktinformasjonEndret(this, utførtAv));
    }

    public void godkjennOppfolgingAvAvtale(NavIdent utførtAv) {
        setOppfolgingVarselSendt(null);
        setKreverOppfolgingFom(getKreverOppfolgingFom().plusMonths(6));
        utforEndring(new OppfolgingAvAvtaleGodkjent(this, utførtAv));
    }

    public void endreStillingsbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        getGjeldendeInnhold().reberegnLønnstilskudd();
        sendTilbakeTilBeslutter();
        utforEndring(new StillingsbeskrivelseEndret(this, utførtAv));
    }

    public void endreOppfølgingOgTilrettelegging(
        EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging,
        NavIdent utførtAv
    ) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        gjeldendeInnhold.setIkrafttredelsestidspunkt(Now.localDateTime());
        sendTilbakeTilBeslutter();
        utforEndring(new OppfølgingOgTilretteleggingEndret(this, utførtAv));
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
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sendTilbakeTilBeslutter();
        utforEndring(new MålEndret(this, utførtAv));
    }

    public void endreInkluderingstilskudd(EndreInkluderingstilskudd endreInkluderingstilskudd, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

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
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sendTilbakeTilBeslutter();
        utforEndring(new InkluderingstilskuddEndret(this, utførtAv));
    }

    public void endreOmMentor(EndreOmMentor endreOmMentor, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
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
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sendTilbakeTilBeslutter();
        utforEndring(new OmMentorEndret(this, utførtAv));
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

    protected LonnstilskuddAvtaleBeregningStrategy hentBeregningStrategi() {
        return this.lonnstilskuddAvtaleBeregningStrategy.updateAndGet(strategy -> strategy == null ? TilskuddsperioderBeregningStrategyFactory.create(
            tiltakstype) : strategy);
    }
}
