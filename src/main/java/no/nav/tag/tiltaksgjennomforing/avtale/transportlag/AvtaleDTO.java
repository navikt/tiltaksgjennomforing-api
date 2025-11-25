package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNrConverter;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.FnrConverter;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneAvArbeidsgiverGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdentConverter;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditerbarEntitet;
import no.nav.tag.tiltaksgjennomforing.oppfolging.Oppfolging;

import java.time.Instant;
import java.time.LocalDate;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@FieldNameConstants
@Builder
public class AvtaleDTO implements AuditerbarEntitet {

    private UUID id;
    @Convert(converter = FnrConverter.class)
    private Fnr deltakerFnr;
    @Convert(converter = FnrConverter.class)
    private Fnr mentorFnr;
    @Convert(converter = BedriftNrConverter.class)
    private BedriftNr bedriftNr;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent veilederNavIdent;

    private Tiltakstype tiltakstype;

    private Instant opprettetTidspunkt;

    private Integer avtaleNr;

    private AvtaleInnholdDTO gjeldendeInnhold;

    private Instant sistEndret;
    private Instant annullertTidspunkt;
    private String annullertGrunn;
    private String enhetGeografisk;
    private String enhetsnavnGeografisk;
    private String enhetOppfolging;
    private String enhetsnavnOppfolging;
    private boolean erRyddeAvtale;

    private Avtaleopphav opphav;

    /**
     * NB: Ønsker ikke å endre status direkte, kall heller .endreAvtale(),
     * som også utfører nødvendige opprydninger.
     */
    @Setter(AccessLevel.NONE)
    private Status status = Status.PÅBEGYNT;

    private boolean godkjentForEtterregistrering;

    @Enumerated(EnumType.STRING)
    private Kvalifiseringsgruppe kvalifiseringsgruppe;
    @Enumerated(EnumType.STRING)
    private Formidlingsgruppe formidlingsgruppe;

    @Nullable
    private TilskuddPeriode gjeldendeTilskuddsperiode;

    private SortedSet<TilskuddPeriode> tilskuddPeriode = new TreeSet<>();
    private boolean feilregistrert;

    @JsonIgnore
    @Transient
    private FnrOgBedrift fnrOgBedrift;

    private LocalDate kreverOppfolgingFom = null;

    private Instant oppfolgingVarselSendt = null;

    public AvtaleDTO(Avtale avtale) {
        this.id = avtale.getId();
        this.deltakerFnr = avtale.getDeltakerFnr();
        this.mentorFnr = avtale.getMentorFnr();
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

    @JsonProperty
    public boolean erUfordelt() {
        return this.getVeilederNavIdent() == null;
    }

    @Override
    public FnrOgBedrift getFnrOgBedrift() {
        return this.fnrOgBedrift;
    }

}
