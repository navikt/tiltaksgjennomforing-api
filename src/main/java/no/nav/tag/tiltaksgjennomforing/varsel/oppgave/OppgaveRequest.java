package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OppgaveRequest {

    private final static String BESKRIVELSE = "Avtale om arbeidstrening";
    private final static String TEMA = "TIL";
    private final static String HOY_PRI = "HOY";
    private final static String OPPG_TYPE = "VURD_HENV";
    private final static String BEHANDLINGSTEMA_ABRIDSTRENING = "ab0160"; //'Arbeidstreningplass'


    private final String beskrivelse = BESKRIVELSE;
    private final String tema = TEMA;
    private final String prioritet = HOY_PRI;
    private final String oppgavetype = OPPG_TYPE;
    private final String behandlingstema = BEHANDLINGSTEMA_ABRIDSTRENING;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate aktivDato = LocalDate.now();

    private final String folkeregisterident;
    private final String aktoerId;
}
