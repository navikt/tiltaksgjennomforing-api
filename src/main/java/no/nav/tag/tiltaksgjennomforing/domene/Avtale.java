package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.Utils.ikkeNull;

@Data
public class Avtale {

    @Id
    private Integer id;
    private final LocalDateTime opprettetTidspunkt;

    private final Fnr deltakerFnr;
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

    private final NavIdent veilederNavIdent;
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
    private List<Maal> maal = new ArrayList<>();
    @Column(keyColumn = "id")
    private List<Oppgave> oppgaver = new ArrayList<>();

    private boolean bekreftetAvBruker;
    private boolean bekreftetAvArbeidsgiver;
    private boolean bekreftetAvVeileder;

    @PersistenceConstructor
    public Avtale(Fnr deltakerFnr, NavIdent veilederNavIdent, LocalDateTime opprettetTidspunkt) {
        this.deltakerFnr = ikkeNull(deltakerFnr, "Deltakers fnr må være satt.");
        this.veilederNavIdent = ikkeNull(veilederNavIdent, "Veileders NAV-ident må være satt.");
        this.opprettetTidspunkt = ikkeNull(opprettetTidspunkt, "Opprettet tidspunkt må være satt.");
    }

    public static Avtale nyAvtale(Fnr deltakerFnr, NavIdent veilederNavIdent) {
        return new Avtale(deltakerFnr, veilederNavIdent, LocalDateTime.now());
    }

    public void endreAvtale(Avtale nyAvtale) {
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
