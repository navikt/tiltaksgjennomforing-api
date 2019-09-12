package no.nav.tag.tiltaksgjennomforing.domene.journalfoering;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvtaleTilJournalfoering {

    public final static String DATE_PATTERN = "dd.MM.yyyy";

    private UUID id;

    private String deltakerFnr;
    private String bedriftNr;
    private String veilederNavIdent;

    private String opprettet;
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
    private Integer arbeidstreningLengde;
    private Integer arbeidstreningStillingprosent;

    private List<MaalTilJournalfoering> maal = new ArrayList<>();
    private List<OppgaveTilJournalFoering> oppgaver = new ArrayList<>();

    private GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn;

    private String godkjentAvDeltaker;
    private String godkjentAvArbeidsgiver;
    private String godkjentAvVeileder;
    private String godkjentPaVegneAv;
}