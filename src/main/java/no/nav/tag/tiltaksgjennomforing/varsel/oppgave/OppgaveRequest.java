package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;

@Data
public class OppgaveRequest {
    private final static String HOY_PRI = "NORM";
    private final static String OPPG_TYPE = "VURD_HENV";

    private final String beskrivelse;
    private final String tema;
    private final String prioritet = HOY_PRI;
    private final String oppgavetype = OPPG_TYPE;
    private final String behandlingstype;
    private final String behandlingstema;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate aktivDato = Now.localDate();
    private final String aktoerId;

    public OppgaveRequest(String aktoerId, GosysTema tema, GosysBehandlingstype behandlingstype, Tiltakstype tiltakstype, String beskrivelse) {
        this.aktoerId = aktoerId;
        this.tema = tema.getTemakode();
        this.behandlingstype = behandlingstype.getBehandlingstypekode();
        this.behandlingstema = tiltakstype.getBehandlingstema();
        this.beskrivelse = beskrivelse;
    }
}
