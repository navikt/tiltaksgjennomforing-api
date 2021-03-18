package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erTom;

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
    private Integer sumLønnstilskuddRedusert;
    private LocalDate datoForRedusertProsent;
    @Enumerated(EnumType.STRING)
    private Stillingstype stillingstype;

    // Arbeidstrening
    @OneToMany(mappedBy = "avtaleInnhold", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<Maal> maal = new ArrayList<>();

    // Godkjenning
    private LocalDateTime godkjentAvDeltaker;
    private LocalDateTime godkjentAvArbeidsgiver;
    private LocalDateTime godkjentAvVeileder;
    @Convert(converter = NavIdentConverter.class)
    private NavIdent godkjentAvNavIdent;

    @Embedded
    private GodkjentPaVegneGrunn godkjentPaVegneGrunn;
    private boolean godkjentPaVegneAv;

    public static AvtaleInnhold nyttTomtInnhold(Tiltakstype tiltakstype) {
        var innhold = new AvtaleInnhold();
        innhold.setId(UUID.randomUUID());
        innhold.setVersjon(1);
        if (tiltakstype == Tiltakstype.SOMMERJOBB) {
            innhold.setStillingstype(Stillingstype.MIDLERTIDIG);
        }
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
                .build();
        nyVersjon.getMaal().forEach(m -> m.setAvtaleInnhold(nyVersjon));
        return nyVersjon;
    }

    public AvtaleInnhold nyGodkjentVersjon() {
        AvtaleInnhold nyVersjon = toBuilder()
                .id(UUID.randomUUID())
                .maal(kopiAvMål())
                .journalpostId(null)
                .versjon(versjon + 1)
                .build();
        nyVersjon.getMaal().forEach(m -> m.setAvtaleInnhold(nyVersjon));
        return nyVersjon;
    }

    private List<Maal> kopiAvMål() {
        return maal.stream().map(m -> new Maal(m)).collect(Collectors.toList());
    }

    void endreAvtale(EndreAvtale nyAvtale) {
        innholdStrategi().endre(nyAvtale);
    }

    boolean erAltUtfylt() {
        return felterSomIkkeErFyltUt().isEmpty();
    }

    public Set<String> felterSomIkkeErFyltUt() {
        return innholdStrategi().alleFelterSomMåFyllesUt()
                .entrySet().stream()
                .filter(entry -> erTom(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private AvtaleInnholdStrategy innholdStrategi() {
        return AvtaleInnholdStrategyFactory.create(this, avtale.getTiltakstype());
    }

    public boolean skalJournalfores() {
        return this.godkjentAvVeileder != null && this.getJournalpostId() == null;
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning tilskuddsberegning) {
        innholdStrategi().endreTilskuddsberegning(tilskuddsberegning);
    }
}


