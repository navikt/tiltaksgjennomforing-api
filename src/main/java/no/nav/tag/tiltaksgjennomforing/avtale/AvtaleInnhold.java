package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import no.nav.tag.tiltaksgjennomforing.exceptions.AvtalensVarighetMerEnnMaksimaltAntallMånederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.StartDatoErEtterSluttDatoException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private static final int MAKSIMALT_ANTALL_MÅNEDER_VARIGHET = 3;

    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "avtale")
    @JsonIgnore
    @ToString.Exclude
    private Avtale avtale;

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

    // Lønnstilskudd
    private String arbeidsgiverKontonummer;
    private String stillingtype;
    private String stillingbeskrivelse;
    private Integer lonnstilskuddProsent;
    private Integer manedslonn;
    private BigDecimal feriepengesats;
    private BigDecimal arbeidsgiveravgift;

    // Arbeidstrening
    @OneToMany(mappedBy = "avtaleInnhold", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Maal> maal = new ArrayList<>();

    @OneToMany(mappedBy = "avtaleInnhold", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oppgave> oppgaver = new ArrayList<>();

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
        return innhold;
    }

    public AvtaleInnhold nyVersjon() {
        AvtaleInnhold nyVersjon = toBuilder()
                .id(UUID.randomUUID())
                .maal(kopiAvMål())
                .oppgaver(kopiAvOppgaver())
                .godkjentAvDeltaker(null)
                .godkjentAvArbeidsgiver(null)
                .godkjentAvVeileder(null)
                .godkjentPaVegneAv(false)
                .godkjentPaVegneGrunn(null)
                .build();
        nyVersjon.getMaal().forEach(m -> m.setAvtaleInnhold(nyVersjon));
        nyVersjon.getOppgaver().forEach(o -> o.setAvtaleInnhold(nyVersjon));
        return nyVersjon;
    }

    private List<Maal> kopiAvMål() {
        return maal.stream().map(m -> new Maal(m)).collect(Collectors.toList());
    }

    private List<Oppgave> kopiAvOppgaver() {
        return oppgaver.stream().map(o -> new Oppgave(o)).collect(Collectors.toList());
    }

    void sjekkStartOgSluttDato(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null) {
            if (startDato.isAfter(sluttDato)) {
                throw new StartDatoErEtterSluttDatoException();
            } else if (sluttDato.isAfter(startDato.plusMonths(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET))) {
                throw new AvtalensVarighetMerEnnMaksimaltAntallMånederException(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET);
            }
        }
    }

    void endreAvtale(EndreAvtale nyAvtale) {
        getStrategy().endre(nyAvtale);
    }

    boolean heleAvtalenErFyltUt() {
        return getStrategy().heleAvtaleUtfylt();
    }

    private AvtaleInnholdStrategy getStrategy() {
        return AvtaletypeStrategyFactory.create(this, avtale.getTiltakstype());
    }
}


