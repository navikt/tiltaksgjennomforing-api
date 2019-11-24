package no.nav.tag.tiltaksgjennomforing.journalfoering;

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
    private UUID id;

    private String deltakerFnr;
    private String bedriftNr;
    private String veilederNavIdent;

    private LocalDate opprettet;
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

    private List<MaalTilJournalfoering> maal = new ArrayList<>();
    private List<OppgaveTilJournalFoering> oppgaver = new ArrayList<>();

    private GodkjentPaVegneGrunnTilJournalfoering godkjentPaVegneGrunn;

    private LocalDate godkjentAvDeltaker;
    private LocalDate godkjentAvArbeidsgiver;
    private LocalDate godkjentAvVeileder;
    private boolean godkjentPaVegneAv;
}