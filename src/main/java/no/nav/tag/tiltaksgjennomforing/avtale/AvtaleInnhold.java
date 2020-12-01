package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

// Lombok
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
// Hibernate
@Entity
public class AvtaleInnhold {

    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "avtale")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

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
    private LocalDate sluttDato;
    private Integer stillingprosent;
    private String journalpostId;
    private String arbeidsoppgaver;
    private String stillingstittel;
    private Integer stillingStyrk08;
    private Integer stillingKonseptId;


    // Mentor
    private String mentorFornavn;
    private String mentorEtternavn;
    private String mentorOppgaver;
    private Integer mentorAntallTimer;
    private Integer mentorTimelonn;

    // Lønnstilskudd
    private String arbeidsgiverKontonummer;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;
    private Boolean harFamilietilknytning;
    private String familietilknytningForklaring;
    private Integer feriepengerBelop;
    private Double otpSats;
    private Integer otpBelop;
    private Integer arbeidsgiveravgiftBelop;
    private Integer sumLonnsutgifter;
    private Integer sumLonnstilskudd;
    private Integer manedslonn100pst;
    @Enumerated(EnumType.STRING)
    private Stillingstype stillingstype;

    @OneToMany(mappedBy = "avtaleInnhold", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TilskuddPeriode> tilskuddPeriode = new ArrayList<>();

    // Arbeidstrening
    @OneToMany(mappedBy = "avtaleInnhold", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Maal> maal = new ArrayList<>();

    // Godkjenning
    private LocalDateTime godkjentAvDeltaker;
    private LocalDateTime godkjentAvArbeidsgiver;
    private LocalDateTime godkjentAvVeileder;
    @Embedded
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;
    private boolean godkjentPaVegneAv;

    public static AvtaleInnhold nyttTomtInnhold() {
        var innhold = new AvtaleInnhold();
        innhold.setId(UUID.randomUUID());
        innhold.setVersjon(1);
        return innhold;
    }

    public UUID getAvtaleInnholdId() {
        return id;
    }

    public AvtaleInnhold nyVersjon() {
        AvtaleInnhold nyVersjon = toBuilder()
            .id(UUID.randomUUID())
            .maal(kopiAvMål())
            .godkjentAvDeltaker(null)
            .godkjentAvArbeidsgiver(null)
            .godkjentAvVeileder(null)
            .godkjentPaVegneAv(false)
            .godkjentPaVegneGrunn(null)
            .journalpostId(null)
            .versjon(versjon + 1)
            .tilskuddPeriode(kopiAvTilskuddPeriode())
            .build();
        nyVersjon.getMaal().forEach(m -> m.setAvtaleInnhold(nyVersjon));
        nyVersjon.getTilskuddPeriode().forEach(tp -> tp.setAvtaleInnhold(nyVersjon));
        return nyVersjon;
    }

    private List<TilskuddPeriode> kopiAvTilskuddPeriode() {
        return tilskuddPeriode.stream().map(periode -> new TilskuddPeriode(periode)).collect(Collectors.toList());
    }

    private List<Maal> kopiAvMål() {
        return maal.stream().map(m -> new Maal(m)).collect(Collectors.toList());
    }

    void endreAvtale(EndreAvtale nyAvtale) {
        innholdStrategi().endre(nyAvtale);
    }

    boolean erAltUtfylt() {
        return innholdStrategi().erAltUtfylt();
    }

    private AvtaleInnholdStrategy innholdStrategi() {
        return AvtaleInnholdStrategyFactory.create(this, avtale.getTiltakstype());
    }

    public boolean skalJournalfores() {
        return this.godkjentAvVeileder != null && this.getJournalpostId() == null;
    }
}


