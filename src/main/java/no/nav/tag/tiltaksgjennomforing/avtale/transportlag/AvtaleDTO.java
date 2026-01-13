package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneAvArbeidsgiverGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.GodkjentPaVegneGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditerbarEntitet;
import no.nav.tag.tiltaksgjennomforing.oppfolging.Oppfolging;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
public record AvtaleDTO(
    UUID id,
    Fnr deltakerFnr,
    Fnr mentorFnr,
    BedriftNr bedriftNr,
    NavIdent veilederNavIdent,
    Tiltakstype tiltakstype,
    Instant opprettetTidspunkt,
    Integer avtaleNr,
    AvtaleInnholdDTO gjeldendeInnhold,
    Instant sistEndret,
    Instant annullertTidspunkt,
    String annullertGrunn,
    String enhetGeografisk,
    String enhetsnavnGeografisk,
    String enhetOppfolging,
    String enhetsnavnOppfolging,
    boolean erRyddeAvtale,
    Avtaleopphav opphav,
    Status status,
    boolean godkjentForEtterregistrering,
    Kvalifiseringsgruppe kvalifiseringsgruppe,
    Formidlingsgruppe formidlingsgruppe,
    SortedSet<TilskuddPeriodeDTO> tilskuddPeriode,
    boolean feilregistrert,
    LocalDate kreverOppfolgingFom,
    Instant oppfolgingVarselSendt,
    Set<String> felterSomIkkeErFyltUt,
    boolean erOpprettetEllerEndretAvArena,
    TilskuddPeriodeDTO gjeldendeTilskuddsperiode,
    @JsonIgnore
    FnrOgBedrift fnrOgBedrift
) implements AuditerbarEntitet {

    public AvtaleDTO(
        Avtale avtale
    ) {
        this(
            avtale.getId(),
            avtale.getDeltakerFnr(),
            avtale.getMentorFnr(),
            avtale.getBedriftNr(),
            avtale.getVeilederNavIdent(),
            avtale.getTiltakstype(),
            avtale.getOpprettetTidspunkt(),
            avtale.getAvtaleNr(),
            new AvtaleInnholdDTO(avtale.getGjeldendeInnhold()),
            avtale.getSistEndret(),
            avtale.getAnnullertTidspunkt(),
            avtale.getAnnullertGrunn(),
            avtale.getEnhetGeografisk(),
            avtale.getEnhetsnavnGeografisk(),
            avtale.getEnhetOppfolging(),
            avtale.getEnhetsnavnOppfolging(),
            avtale.erRyddeAvtale(),
            avtale.getOpphav(),
            avtale.getStatus(),
            avtale.isGodkjentForEtterregistrering(),
            avtale.getKvalifiseringsgruppe(),
            avtale.getFormidlingsgruppe(),
            avtale.getTilskuddPeriode()
                .stream()
                .map(TilskuddPeriodeDTO::new)
                .collect(Collectors.toCollection(TreeSet::new)),
            avtale.isFeilregistrert(),
            avtale.getKreverOppfolgingFom(),
            avtale.getOppfolgingVarselSendt(),
            avtale.felterSomIkkeErFyltUt(),
            avtale.erOpprettetEllerEndretAvArena(),
            avtale.getGjeldendeTilskuddsperiode() == null ? null : new TilskuddPeriodeDTO(avtale.getGjeldendeTilskuddsperiode()),
            new FnrOgBedrift(avtale.getDeltakerFnr(), avtale.getBedriftNr())
        );
    }


    @JsonProperty
    public boolean erLaast() {
        return erGodkjentAvVeileder() && erGodkjentAvArbeidsgiver() && erGodkjentAvDeltaker();
    }

    @JsonProperty
    public boolean erGodkjentAvDeltaker() {
        return gjeldendeInnhold.godkjentAvDeltaker() != null;
    }

    @JsonProperty
    public boolean erGodkjentTaushetserklæringAvMentor() {
        if (gjeldendeInnhold == null) {
            return false;
        }
        return gjeldendeInnhold.godkjentTaushetserklæringAvMentor() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvArbeidsgiver() {
        return gjeldendeInnhold.godkjentAvArbeidsgiver() != null;
    }

    @JsonProperty
    public boolean erGodkjentAvVeileder() {
        return gjeldendeInnhold.godkjentAvVeileder() != null;
    }

    @JsonProperty
    public boolean erAvtaleInngått() {
        return gjeldendeInnhold.avtaleInngått() != null;
    }

    @JsonProperty
    public Instant godkjentAvDeltaker() {
        return gjeldendeInnhold.godkjentAvDeltaker();
    }

    @JsonProperty
    public Instant godkjentAvMentor() {
        return gjeldendeInnhold.godkjentTaushetserklæringAvMentor();
    }

    @JsonProperty
    public Instant godkjentAvArbeidsgiver() {
        return gjeldendeInnhold.godkjentAvArbeidsgiver();
    }

    @JsonProperty
    public Instant godkjentAvVeileder() {
        return gjeldendeInnhold.godkjentAvVeileder();
    }

    @JsonProperty
    public Instant godkjentAvBeslutter() {
        return gjeldendeInnhold.godkjentAvBeslutter();
    }

    @JsonProperty
    Instant avtaleInngått() {
        return gjeldendeInnhold.avtaleInngått();
    }

    @JsonProperty
    NavIdent godkjentAvNavIdent() {
        return gjeldendeInnhold.godkjentAvNavIdent();
    }

    @JsonProperty
    NavIdent godkjentAvBeslutterNavIdent() {
        return gjeldendeInnhold.godkjentAvBeslutterNavIdent();
    }

    @JsonProperty
    GodkjentPaVegneGrunn godkjentPaVegneGrunn() {
        return gjeldendeInnhold.godkjentPaVegneGrunn();
    }

    @JsonProperty
    boolean godkjentPaVegneAv() {
        return gjeldendeInnhold.godkjentPaVegneAv();
    }

    @JsonProperty
    GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn() {
        return gjeldendeInnhold.godkjentPaVegneAvArbeidsgiverGrunn();
    }

    @JsonProperty
    boolean godkjentPaVegneAvArbeidsgiver() {
        return gjeldendeInnhold().godkjentPaVegneAvArbeidsgiver();
    }

    @JsonProperty
    public LocalDate getKreverOppfolgingFrist() {
        return Oppfolging.fra(this).getOppfolgingsfrist();
    }

    @JsonProperty
    public boolean erUfordelt() {
        return this.veilederNavIdent() == null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Instant getSistEndret() {
        return sistEndret;
    }

    @Override
    @JsonIgnore
    public FnrOgBedrift getFnrOgBedrift() {
        return fnrOgBedrift;
    }

    public AvtaleDTO maskerFelterForAvtalePart(Avtalepart avtalepart) {
        return avtalepart.maskerFelterForAvtalepart(this);
    }
}
