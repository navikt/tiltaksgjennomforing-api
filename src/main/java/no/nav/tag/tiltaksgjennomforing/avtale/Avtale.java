package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.sjekkAtIkkeNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy.StartOgSluttDatoStrategyFactory;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltMåVæreFyltUtException;
import no.nav.tag.tiltaksgjennomforing.exceptions.ArbeidsgiverSkalGodkjenneFørVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.AvtaleErIkkeFordeltException;
import no.nav.tag.tiltaksgjennomforing.exceptions.DeltakerHarGodkjentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.persondata.Navn;
import no.nav.tag.tiltaksgjennomforing.persondata.NavnFormaterer;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.utils.TelefonnummerValidator;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.hibernate.type.PostgresUUIDType;
import org.springframework.data.domain.AbstractAggregateRoot;

@Slf4j
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@FieldNameConstants
@TypeDef(name="postgres-uuid",
        defaultForType = UUID.class,
        typeClass = PostgresUUIDType.class)
public class Avtale extends AbstractAggregateRoot<Avtale> {

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
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Generated(GenerationTime.INSERT)
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
    private boolean opprettetAvArbeidsgiver;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;


    private boolean godkjentForEtterregistrering;

    @Enumerated(EnumType.STRING)
    private Kvalifiseringsgruppe kvalifiseringsgruppe;
    @Enumerated(EnumType.STRING)
    private Formidlingsgruppe formidlingsgruppe;


    @OneToMany(mappedBy = "avtale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @SortNatural
    private SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    private boolean feilregistrert;


    private Avtale(OpprettAvtale opprettAvtale) {
        sjekkAtIkkeNull(opprettAvtale.getDeltakerFnr(), "Deltakers fnr må være satt.");
        sjekkAtIkkeNull(opprettAvtale.getBedriftNr(), "Arbeidsgivers bedriftnr må være satt.");
        if (opprettAvtale.getDeltakerFnr().erUnder16år()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_IKKE_GAMMEL_NOK);
        }
        if (opprettAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && opprettAvtale.getDeltakerFnr().erOver30årFørsteJanuar()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL);
        }

        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = Now.localDateTime();
        this.deltakerFnr = opprettAvtale.getDeltakerFnr();
        this.bedriftNr = opprettAvtale.getBedriftNr();
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
        if (opprettMentorAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB && opprettMentorAvtale.getDeltakerFnr().erOver30årFørsteJanuar()) {
            throw new FeilkodeException(Feilkode.SOMMERJOBB_FOR_GAMMEL);
        }

        this.id = UUID.randomUUID();
        this.opprettetTidspunkt = Now.localDateTime();
        this.deltakerFnr = opprettMentorAvtale.getDeltakerFnr();
        this.mentorFnr = opprettMentorAvtale.getMentorFnr();
        this.bedriftNr = opprettMentorAvtale.getBedriftNr();
        this.tiltakstype = opprettMentorAvtale.getTiltakstype();
        this.sistEndret = Now.instant();
        this.gjeldendeInnhold = AvtaleInnhold.nyttTomtInnhold(tiltakstype);
        this.gjeldendeInnhold.setAvtale(this);
    }

    public static Avtale veilederOppretterAvtale(OpprettAvtale opprettAvtale, NavIdent navIdent) {
        Avtale avtale = new Avtale(opprettAvtale);
        avtale.veilederNavIdent = sjekkAtIkkeNull(navIdent, "Veileders NAV-ident må være satt.");
        avtale.registerEvent(new AvtaleOpprettetAvVeileder(avtale, navIdent));
        return avtale;
    }
    public static Avtale veilederOppretterAvtale(OpprettMentorAvtale opprettMentorAvtale, NavIdent navIdent) {
        Avtale avtale = new Avtale(opprettMentorAvtale);
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
    public static Avtale arbeidsgiverOppretterAvtale(OpprettMentorAvtale opprettMentorAvtale) {
        Avtale avtale = new Avtale(opprettMentorAvtale);
        avtale.opprettetAvArbeidsgiver = true;
        avtale.registerEvent(new AvtaleOpprettetAvArbeidsgiver(avtale));
        return avtale;
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale nyAvtale, Avtalerolle utfortAvRolle, EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder, List<BedriftNr> pilotvirksomheter, Identifikator identifikator) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAvtalenKanEndres();
        sjekkSistEndret(sistEndret);
        sjekkStartOgSluttDato(nyAvtale.getStartDato(), nyAvtale.getSluttDato());
        getGjeldendeInnhold().endreAvtale(nyAvtale);
        if (tiltakstyperMedTilskuddsperioder.contains(tiltakstype) || erPilotVirksomhet(pilotvirksomheter)) {
            nyeTilskuddsperioder();
        }
        sistEndretNå();
        registerEvent(new AvtaleEndret(this, utfortAvRolle, identifikator));
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale nyAvtale, Avtalerolle utfortAv, EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder, List<BedriftNr> pilotvirksomheter) {
        endreAvtale(sistEndret, nyAvtale, utfortAv, tiltakstyperMedTilskuddsperioder, pilotvirksomheter, null);
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
    public boolean erLaast() {
        return erGodkjentAvVeileder() && erGodkjentAvArbeidsgiver() && erGodkjentAvDeltaker();
    }

    @JsonProperty
    public boolean erGodkjentAvDeltaker() {
        return gjeldendeInnhold.getGodkjentAvDeltaker() != null;
    }

    @JsonProperty
    public boolean erGodkjentTaushetserklæringAvMentor() {
        if(gjeldendeInnhold == null) return false;
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
    public LocalDateTime godkjentAvDeltaker() { return gjeldendeInnhold.getGodkjentAvDeltaker();}

    @JsonProperty
    public LocalDateTime godkjentAvMentor() { return gjeldendeInnhold.getGodkjentTaushetserklæringAvMentor();}

    @JsonProperty
    public LocalDateTime godkjentAvArbeidsgiver() { return gjeldendeInnhold.getGodkjentAvArbeidsgiver();}
    @JsonProperty
    public LocalDateTime godkjentAvVeileder() { return gjeldendeInnhold.getGodkjentAvVeileder();}
    @JsonProperty
    public LocalDateTime godkjentAvBeslutter() { return gjeldendeInnhold.getGodkjentAvBeslutter();}
    @JsonProperty
    private LocalDateTime avtaleInngått() {return gjeldendeInnhold.getAvtaleInngått();}
    @JsonProperty
    private NavIdent godkjentAvNavIdent() {return gjeldendeInnhold.getGodkjentAvNavIdent();}
    @JsonProperty
    private NavIdent godkjentAvBeslutterNavIdent() {return gjeldendeInnhold.getGodkjentAvBeslutterNavIdent();}
    @JsonProperty
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn() {return gjeldendeInnhold.getGodkjentPaVegneGrunn();}
    @JsonProperty
    private boolean godkjentPaVegneAv() {return gjeldendeInnhold.isGodkjentPaVegneAv();}
    @JsonProperty
    private GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn() {return gjeldendeInnhold.getGodkjentPaVegneAvArbeidsgiverGrunn();}
    @JsonProperty
    private boolean godkjentPaVegneAvArbeidsgiver() {return gjeldendeInnhold.isGodkjentPaVegneAvArbeidsgiver();}



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
        gjeldendeInnhold.setGodkjentAvDeltaker(null);
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(null);
        gjeldendeInnhold.setGodkjentAvVeileder(null);
        gjeldendeInnhold.setGodkjentPaVegneAv(false);
        gjeldendeInnhold.setGodkjentPaVegneGrunn(null);
        gjeldendeInnhold.setGodkjentAvNavIdent(null);
        sistEndretNå();
    }

    private void sistEndretNå() {
        this.sistEndret = Now.instant();
    }

    void sjekkSistEndret(Instant sistEndret) {
        if (sistEndret == null || sistEndret.isBefore(this.sistEndret)) {
            throw new SamtidigeEndringerException();
        }
    }
    //TODO TEST MEG
    void godkjennForArbeidsgiver(Identifikator utfortAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvArbeidsgiver()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_ARBEIDSGIVER_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentAvArbeidsgiver(Now.localDateTime());
        sistEndretNå();
        registerEvent(new GodkjentAvArbeidsgiver(this, utfortAv));
    }

    private Boolean erPilotVirksomhet(List<BedriftNr> pilotvirksomheter) {
        return pilotvirksomheter.contains(bedriftNr) && (tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    void godkjennForVeileder(NavIdent utfortAv, List<BedriftNr> pilotvirksomheter) {
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
        }
        if (this.getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD && this.getDeltakerFnr().erOver72ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        } else if (this.getTiltakstype() != Tiltakstype.VARIG_LONNSTILSKUDD && this.getDeltakerFnr().erOver67ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_67_AAR);
        }

        LocalDateTime tidspunkt = Now.localDateTime();
        gjeldendeInnhold.setGodkjentAvVeileder(tidspunkt);
        gjeldendeInnhold.setGodkjentAvNavIdent(new NavIdent(utfortAv.asString()));
        if (tiltakstype != Tiltakstype.SOMMERJOBB && !erPilotVirksomhet(pilotvirksomheter)) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        gjeldendeInnhold.setIkrafttredelsestidspunkt(tidspunkt);
        sistEndretNå();
        registerEvent(new GodkjentAvVeileder(this, utfortAv));
    }

    private void avtaleInngått(LocalDateTime tidspunkt, Avtalerolle utførtAvRolle, NavIdent utførtAv) {
        gjeldendeInnhold.setAvtaleInngått(tidspunkt);
        registerEvent(new AvtaleInngått(this, utførtAvRolle, utførtAv));
    }
    void godkjennForVeilederOgDeltaker(NavIdent utfortAv, GodkjentPaVegneGrunn paVegneAvGrunn, List<BedriftNr> pilotvirksomheter) {
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
        }
        if (this.getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD && this.getDeltakerFnr().erOver72ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
            throw new FeilkodeException(Feilkode.DELTAKER_72_AAR);
        } else if (this.getTiltakstype() != Tiltakstype.VARIG_LONNSTILSKUDD && this.getDeltakerFnr().erOver67ÅrFraSluttDato(getGjeldendeInnhold().getSluttDato())) {
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
        if (tiltakstype != Tiltakstype.SOMMERJOBB  && !erPilotVirksomhet(pilotvirksomheter)) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAvDeltaker(this, utfortAv));
    }
    void godkjennForVeilederOgArbeidsgiver(NavIdent utfortAv, GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn, List<BedriftNr> pilotvirksomheter) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
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
        if (this.getDeltakerFnr().erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
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
        if (tiltakstype != Tiltakstype.SOMMERJOBB && !erPilotVirksomhet(pilotvirksomheter)) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAvArbeidsgiver(this, utfortAv));
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(NavIdent utfortAv, GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn, List<BedriftNr> pilotvirksomheter) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        sjekkOmAltErKlarTilGodkjenning();
        if (tiltakstype != Tiltakstype.SOMMERJOBB) {
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
        if (this.getDeltakerFnr().erOver30årFraOppstartDato(getGjeldendeInnhold().getStartDato())) {
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
        if (tiltakstype != Tiltakstype.SOMMERJOBB && !erPilotVirksomhet(pilotvirksomheter)) {
            avtaleInngått(tidspunkt, Avtalerolle.VEILEDER, utfortAv);
        }
        sistEndretNå();
        registerEvent(new GodkjentPaVegneAvDeltakerOgArbeidsgiver(this, utfortAv));
    }

    void godkjennForDeltaker(Identifikator utfortAv) {
        sjekkOmAltErKlarTilGodkjenning();
        if (erGodkjentAvDeltaker()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_DELTAKER_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentAvDeltaker(Now.localDateTime());
        sistEndretNå();
        registerEvent(new GodkjentAvDeltaker(this, utfortAv));
    }

    void godkjennForMentor(Identifikator utfortAv) {
        if (erGodkjentTaushetserklæringAvMentor()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_MENTOR_HAR_ALLEREDE_GODKJENT);
        }
        gjeldendeInnhold.setGodkjentTaushetserklæringAvMentor(Now.localDateTime());
        sistEndretNå();
        registerEvent(new SignertAvMentor(this, utfortAv));
    }

    void sjekkOmAltErKlarTilGodkjenning() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erAltUtfylt()) {
            throw new AltMåVæreFyltUtException();
        }
        if (List.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB).contains(tiltakstype) &&
                Utils.erNoenTomme(gjeldendeInnhold.getSumLonnstilskudd(), gjeldendeInnhold.getLonnstilskuddProsent())) {
            throw new FeilkodeException(Feilkode.MANGLER_BEREGNING);
        }
        if (veilederNavIdent == null) {
            throw new FeilkodeException(Feilkode.MANGLER_VEILEDER_PÅ_AVTALE);
        }
    }

    @JsonProperty
    public String status() {
        return statusSomEnum().getBeskrivelse();
    }

    @JsonProperty
    public Status statusSomEnum() {
        if (getAnnullertTidspunkt() != null) {
            return Status.ANNULLERT;
        } else if (isAvbrutt()) {
            return Status.AVBRUTT;
        } else if (erAvtaleInngått() && (gjeldendeInnhold.getSluttDato().isBefore(Now.localDate()))) {
            return Status.AVSLUTTET;
        } else if (erAvtaleInngått() && (gjeldendeInnhold.getStartDato().isBefore(Now.localDate().plusDays(1)))) {
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
        sjekkAtIkkeAvtalenInneholderUtbetaltTilskuddsperiode();
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        annullerTilskuddsperioder();
        setAnnullertTidspunkt(Now.instant());
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

    private void sjekkAtIkkeAvtalenInneholderUtbetaltTilskuddsperiode() {
        if(this.getTilskuddPeriode().stream().anyMatch(TilskuddPeriode::erUtbetalt)) throw new FeilkodeException(Feilkode.AVTALE_INNEHOLDER_UTBETALT_TILSKUDDSPERIODE);
        if(this.getTilskuddPeriode().stream().anyMatch(TilskuddPeriode::erRefusjonGodkjent)) throw new FeilkodeException(Feilkode.AVTALE_INNEHOLDER_TILSKUDDSPERIODE_MED_GODKJENT_REFUSJON);
    }

    public void overtaAvtale(NavIdent nyNavIdent) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        NavIdent gammelNavIdent = this.getVeilederNavIdent();
        this.setVeilederNavIdent(nyNavIdent);
        getGjeldendeInnhold().reberegnLønnstilskudd();
        sistEndretNå();
        if (gammelNavIdent == null) {
            this.registerEvent(new AvtaleOpprettetAvArbeidsgiverErFordelt(this));
        } else {
            registerEvent(new AvtaleNyVeileder(this, gammelNavIdent));
        }
    }

    private boolean erAltUtfylt() {
        return felterSomIkkeErFyltUt().isEmpty();
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

    private void annullerTilskuddsperiode(TilskuddPeriode tilskuddsperiode) {
        // Sjekk på refusjonens status
        if(tilskuddsperiode.getRefusjonStatus() == RefusjonStatus.UTGÅTT) {
            log.warn("Sender ikke annuleringsmelding for tilskuddsperiode {} med utgått refusjon.", tilskuddsperiode.getId());
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
        TilskuddPeriode gjeldendePeriode = gjeldendeTilskuddsperiode();
        gjeldendePeriode.godkjenn(beslutter, enhet);
        if (!erAvtaleInngått()) {
            LocalDateTime tidspunkt = Now.localDateTime();
            godkjennForBeslutter(tidspunkt, beslutter);
            avtaleInngått(tidspunkt, Avtalerolle.BESLUTTER, beslutter);
        }
        sistEndretNå();
        registerEvent(new TilskuddsperiodeGodkjent(this, gjeldendePeriode, beslutter));
    }

    private void godkjennForBeslutter(LocalDateTime tidspunkt, NavIdent beslutter) {
        gjeldendeInnhold.setGodkjentAvBeslutter(tidspunkt);
        gjeldendeInnhold.setGodkjentAvBeslutterNavIdent(beslutter);
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

    public void togglegodkjennEtterregistrering(NavIdent beslutter) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_MERKES_FOR_ETTERREGISTRERING_AVTALE_GODKJENT);
        }
        setGodkjentForEtterregistrering(!this.godkjentForEtterregistrering);
        sistEndretNå();
        if (this.godkjentForEtterregistrering) {
            registerEvent(new GodkjentForEtterregistrering(this, beslutter));
        } else {
            registerEvent(new FjernetEtterregistrering(this, beslutter));
        }
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
                .filter(TilskuddPeriode::isAktiv)
                .filter(t -> t.getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
                .map(TilskuddPeriode::deaktiverOgLagNyUbehandlet).toList();
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
        return RegnUtTilskuddsperioderForAvtale.beløpForPeriode(startDato,
                sluttDato,
                gjeldendeInnhold.getDatoForRedusertProsent(),
                gjeldendeInnhold.getSumLonnstilskudd(),
                gjeldendeInnhold.getSumLønnstilskuddRedusert());
    }

    private List<TilskuddPeriode> beregnTilskuddsperioder(LocalDate startDato, LocalDate sluttDato) {
        List<TilskuddPeriode> tilskuddsperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(
            tiltakstype,
            gjeldendeInnhold.getSumLonnstilskudd(),
                startDato,
                sluttDato,
                gjeldendeInnhold.getLonnstilskuddProsent(),
                gjeldendeInnhold.getDatoForRedusertProsent(),
                gjeldendeInnhold.getSumLønnstilskuddRedusert());
        tilskuddsperioder.forEach(t -> t.setAvtale(this));
        tilskuddsperioder.forEach(t -> t.setEnhet(gjeldendeInnhold.getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(gjeldendeInnhold.getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
    }

    private void nyeTilskuddsperioder() {
        if (erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        tilskuddPeriode.removeIf(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET);
        if (Utils.erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato(), gjeldendeInnhold.getSumLonnstilskudd())) {
            List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioder(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());

            fikseLøpenumre(tilskuddsperioder, 1);
            tilskuddPeriode.addAll(tilskuddsperioder);
        }
    }

    private boolean sjekkArenaMigrering() {
        if(!tilskuddPeriode.isEmpty()) {
            return false;
        }
        // Statuser som skal få tilskuddsperioder
        Status status = statusSomEnum();
        if(status == Status.ANNULLERT || status == Status.AVSLUTTET || status == Status.AVBRUTT) {
            return false;
        }

        return true;
    }
    /**
     * Avtaler (lønnstilskudd) som avsluttes i Arena må få tilskuddsperioder her.
     *
     * - Sjekk at avtalen ikke allerede har perioder (altså en pilotavtale)
     * - Tilskuddsperioder lages fra startdato til sluttdato, de som er før dato for migrering settes til en ny status, f eks. BEHANDLET_I_ARENA
     * - Sjekk logikk som skjer ved godkjenning av første perioden
     * - Tar ikke høyde for perioder med lengde tre måneder som i arena
     * -
     */
    public boolean nyeTilskuddsperioderVedMigreringFraArena(LocalDate migreringsDato) {
        if(sjekkArenaMigrering()) {
            List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioder(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
            tilskuddsperioder.forEach(periode -> {
                // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                if(periode.getSluttDato().minusDays(1).isBefore(migreringsDato)) {
                    periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                }
            });
            fikseLøpenumre(tilskuddsperioder, 1);
            tilskuddPeriode.addAll(tilskuddsperioder);
            return true;
        } else {
            log.info("Avtale {} har allerede tilskuddsperioder, genererer ikke nye", id);
            return false;
        }
    }

    public void forkortAvtale(LocalDate nySluttDato, String grunn, String annetGrunn, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_IKKE_GODKJENT_AVTALE);
        }
        if (!nySluttDato.isBefore(gjeldendeInnhold.getSluttDato())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_ETTER_SLUTTDATO);
        }
        sjekkStartOgSluttDato(gjeldendeInnhold.getStartDato(), nySluttDato);
        if (StringUtils.isBlank(grunn) || (grunn.equals("Annet") && StringUtils.isBlank(annetGrunn))) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_FORKORTE_GRUNN_MANGLER);
        }
        AvtaleInnhold nyAvtaleInnholdVersjon = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORKORTE);
        gjeldendeInnhold = nyAvtaleInnholdVersjon;
        getGjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
        forkortTilskuddsperioder(nySluttDato);
        registerEvent(new AvtaleForkortet(this, nyAvtaleInnholdVersjon, nySluttDato, grunn, annetGrunn, utførtAv));
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
        AvtaleInnhold nyVersjon = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.FORLENGE);
        gjeldendeInnhold = nyVersjon;
        getGjeldendeInnhold().endreSluttDato(nySluttDato);
        sendTilbakeTilBeslutter();
        forlengTilskuddsperioder(gammelSluttDato, nySluttDato);
        sistEndretNå();
        registerEvent(new AvtaleForlenget(this, utførtAv));
    }



    private void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        StartOgSluttDatoStrategyFactory.create(getTiltakstype(), getKvalifiseringsgruppe()).sjekkStartOgSluttDato(startDato, sluttDato, isGodkjentForEtterregistrering(), erAvtaleInngått());
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
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_TILSKUDDSBEREGNING);
        getGjeldendeInnhold().endreTilskuddsberegning(tilskuddsberegning);
        endreBeløpITilskuddsperioder();
        sistEndretNå();
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        registerEvent(new TilskuddsberegningEndret(this, utførtAv));
    }

    // Metode for å rydde opp i beregnede felter som ikke har blitt satt etter at lønnstilskuddsprosent manuelt i databasen har blitt satt inn
    public void reberegnLønnstilskudd() {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        krevEnAvTiltakstyper(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB);
        if (gjeldendeInnhold.getSumLonnstilskudd() == null && Utils.erIkkeTomme(
                gjeldendeInnhold.getLonnstilskuddProsent(),
                gjeldendeInnhold.getArbeidsgiveravgift(),
                gjeldendeInnhold.getFeriepengesats(),
                gjeldendeInnhold.getManedslonn(),
                gjeldendeInnhold.getOtpSats())) {
            getGjeldendeInnhold().reberegnLønnstilskudd();
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

        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_KONTAKTINFO);
        getGjeldendeInnhold().endreKontaktInfo(endreKontaktInformasjon);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
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
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_STILLING);
        getGjeldendeInnhold().endreStillingsInfo(endreStillingsbeskrivelse);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
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
        gjeldendeInnhold = gjeldendeInnhold.nyGodkjentVersjon(AvtaleInnholdType.ENDRE_OPPFØLGING_OG_TILRETTELEGGING);
        gjeldendeInnhold.endreOppfølgingOgTilretteleggingInfo(endreOppfølgingOgTilrettelegging);
        gjeldendeInnhold.setIkrafttredelsestidspunkt(Now.localDateTime());
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
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_MÅL);
        getGjeldendeInnhold().getMaal().clear();
        List<Maal> nyeMål = endreMål.getMaal().stream().map(m -> new Maal().setId(UUID.randomUUID()).setBeskrivelse(m.getBeskrivelse()).setKategori(m.getKategori())).collect(Collectors.toList());
        getGjeldendeInnhold().getMaal().addAll(nyeMål);
        getGjeldendeInnhold().getMaal().forEach(m -> m.setAvtaleInnhold(getGjeldendeInnhold()));
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new MålEndret(this, utførtAv));
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
        if (endreInkluderingstilskudd.inkluderingstilskuddTotalBeløp() > 136000) {
            throw new FeilkodeException(Feilkode.INKLUDERINGSTILSKUDD_SUM_FOR_HØY);
        }
        for (Inkluderingstilskuddsutgift i : endreInkluderingstilskudd.getInkluderingstilskuddsutgift()) {
            if (Utils.erNoenTomme(i.getBeløp(), i.getType())) {
                throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_IKKE_BELOP_ELLER_TYPE);
            }
        }
        List<Inkluderingstilskuddsutgift> inkluderingstilskuddsutgifterPåForrigeVersjon = getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        List<Inkluderingstilskuddsutgift> forrigeVersjonFraKlient = endreInkluderingstilskudd.getInkluderingstilskuddsutgift().stream().filter(e -> e.getId() != null).collect(Collectors.toList());

        // Sjekk at det er like mange utgifter på forrige versjon som det er id'er i request. Hvis ikke er ikke frontend i sync
        if(inkluderingstilskuddsutgifterPåForrigeVersjon.size() != forrigeVersjonFraKlient.size()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_TOM_LISTE);
        }
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_INKLUDERINGSTILSKUDD);

        List<Inkluderingstilskuddsutgift> nye = endreInkluderingstilskudd.getInkluderingstilskuddsutgift().stream().filter(e -> e.getId() == null).collect(Collectors.toList());
        List<Inkluderingstilskuddsutgift> nyeInkluderingstilskuddsutgifter = nye.stream().map(m -> new Inkluderingstilskuddsutgift().setId(UUID.randomUUID()).setBeløp(m.getBeløp()).setType(m.getType())).collect(Collectors.toList());

        getGjeldendeInnhold().getInkluderingstilskuddsutgift().addAll(nyeInkluderingstilskuddsutgifter);
        getGjeldendeInnhold().getInkluderingstilskuddsutgift().forEach(i -> i.setAvtaleInnhold(getGjeldendeInnhold()));
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new InkluderingstilskuddEndret(this, utførtAv));
    }

    public void endreOmMentor(EndreOmMentor endreOmMentor, NavIdent utførtAv) {
        sjekkAtIkkeAvtaleErAnnullertEllerAvbrutt();
        krevEnAvTiltakstyper(Tiltakstype.MENTOR);

        if (!erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_IKKE_INNGAATT_AVTALE);
        }
        if (Utils.erNoenTomme(endreOmMentor.getMentorFornavn(), endreOmMentor.getMentorEtternavn(),
                endreOmMentor.getMentorTlf(), endreOmMentor.getMentorTimelonn(),
                endreOmMentor.getMentorAntallTimer(), endreOmMentor.getMentorOppgaver())) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_OM_MENTOR_UGYLDIG_INPUT);
        }
        gjeldendeInnhold = getGjeldendeInnhold().nyGodkjentVersjon(AvtaleInnholdType.ENDRE_OM_MENTOR);
        getGjeldendeInnhold().endreOmMentor(endreOmMentor);
        getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        sistEndretNå();
        sendTilbakeTilBeslutter();
        registerEvent(new OmMentorEndret(this, utførtAv));
    }

}
