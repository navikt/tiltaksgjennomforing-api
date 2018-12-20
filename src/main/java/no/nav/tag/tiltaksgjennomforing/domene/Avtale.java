package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.TiltaksgjennomforingException;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Avtale {

    @Id
    private Integer id;
    private LocalDateTime opprettetTidspunkt;

    private Fnr deltakerFnr;
    private String deltakerFornavn;
    private String deltakerEtternavn;
    private String deltakerAdresse;
    private String deltakerPostnummer;
    private String deltakerPoststed;

    private String bedriftNavn;
    private String bedriftAdresse;
    private String bedriftPostnummer;
    private String bedriftPoststed;

    private Fnr arbeidsgiverFnr;
    private String arbeidsgiverFornavn;
    private String arbeidsgiverEtternavn;
    private String arbeidsgiverEpost;
    private String arbeidsgiverTlf;

    private NavIdent veilederNavIdent;
    private String veilederFornavn;
    private String veilederEtternavn;
    private String veilederEpost;
    private String veilederTlf;

    private String oppfolging;
    private String tilrettelegging;

    private LocalDateTime startDatoTidspunkt;
    private Integer arbeidstreningLengde;
    private Integer arbeidstreningStillingprosent;

    @Column(keyColumn = "id")
    private List<Maal> maal;
    @Column(keyColumn = "id")
    private List<Oppgave> oppgaver;

    private boolean bekreftetAvBruker;
    private boolean bekreftetAvArbeidsgiver;
    private boolean bekreftetAvVeileder;

    public static AvtaleBuilder builder() {
        return new AvtaleBuilder() {
            public Avtale build() {
                if (super.deltakerFnr == null || super.veilederNavIdent == null) {
                    throw new TiltaksgjennomforingException("Identitet til enten deltaker eller " +
                            "veileder er null.");
                }
                return super.build();
            }
        };
    }

    public static Avtale nyAvtale(Fnr deltakerFnr, NavIdent veilederNavIdent) {
        return Avtale.builder()
                .deltakerFnr(deltakerFnr)
                .veilederNavIdent(veilederNavIdent)
                .opprettetTidspunkt(LocalDateTime.now())
                .maal(new ArrayList<>())
                .oppgaver(new ArrayList<>())
                .deltakerFornavn("")
                .deltakerEtternavn("")
                .deltakerAdresse("")
                .deltakerPostnummer("")
                .deltakerPoststed("")
                .bedriftNavn("")
                .bedriftAdresse("")
                .bedriftPostnummer("")
                .bedriftPoststed("")
                .arbeidsgiverFornavn("")
                .arbeidsgiverEtternavn("")
                .arbeidsgiverEpost("")
                .arbeidsgiverTlf("")
                .veilederFornavn("")
                .veilederEtternavn("")
                .veilederEpost("")
                .veilederTlf("")
                .oppfolging("")
                .tilrettelegging("")
                .startDatoTidspunkt(LocalDateTime.now())
                .arbeidstreningLengde(1)
                .arbeidstreningStillingprosent(0)
                .bekreftetAvBruker(false)
                .bekreftetAvArbeidsgiver(false)
                .bekreftetAvVeileder(false)
                .build();
    }

    public void endreAvtale(Avtale nyAvtale) {
        setDeltakerFnr(nyAvtale.deltakerFnr);
        setDeltakerFornavn(nyAvtale.deltakerFornavn);
        setDeltakerEtternavn(nyAvtale.deltakerEtternavn);
        setDeltakerAdresse(nyAvtale.deltakerAdresse);
        setDeltakerPostnummer(nyAvtale.deltakerPostnummer);
        setDeltakerPoststed(nyAvtale.deltakerPoststed);

        setBedriftNavn(nyAvtale.bedriftNavn);
        setBedriftAdresse(nyAvtale.bedriftAdresse);
        setBedriftPostnummer(nyAvtale.bedriftPostnummer);
        setBedriftPoststed(nyAvtale.bedriftPoststed);

        setArbeidsgiverFnr(nyAvtale.arbeidsgiverFnr);
        setArbeidsgiverFornavn(nyAvtale.arbeidsgiverFornavn);
        setArbeidsgiverEtternavn(nyAvtale.arbeidsgiverEtternavn);
        setArbeidsgiverEpost(nyAvtale.arbeidsgiverEpost);
        setArbeidsgiverTlf(nyAvtale.arbeidsgiverTlf);

        setVeilederFornavn(nyAvtale.veilederFornavn);
        setVeilederEtternavn(nyAvtale.veilederEtternavn);
        setVeilederEpost(nyAvtale.veilederEpost);
        setVeilederTlf(nyAvtale.veilederTlf);

        setOppfolging(nyAvtale.oppfolging);
        setTilrettelegging(nyAvtale.tilrettelegging);
        setStartDatoTidspunkt(nyAvtale.startDatoTidspunkt);
        setArbeidstreningLengde(nyAvtale.arbeidstreningLengde);
        setArbeidstreningStillingprosent(nyAvtale.arbeidstreningStillingprosent);

        setMaal(nyAvtale.maal);
        maal.forEach(Maal::setterOppretterTidspunkt);
        setOppgaver(nyAvtale.oppgaver);
        oppgaver.forEach(Oppgave::setterOppretterTidspunkt);
    }
}
