package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Value
class OppgaveRequest {
    String beskrivelse;
    String tema;
    String prioritet = "NORM";
    String oppgavetype = "VURD_HENV";
    String behandlingstype;
    String behandlingstema;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate aktivDato = Now.localDate();
    String aktoerId;

    public OppgaveRequest(
            @NotNull String aktoerId,
            @NotNull GosysTema tema,
            @NotNull GosysBehandlingstype behandlingstype,
            @Nullable Tiltakstype tiltakstype,
            @NotNull String beskrivelse
    ) {
        this.aktoerId = aktoerId;
        this.tema = tema.getTemakode();
        this.behandlingstype = behandlingstype.getBehandlingstypekode();
        this.behandlingstema = tiltakstype == null ? null : tiltakstype.getBehandlingstema();
        this.beskrivelse = beskrivelse;
    }
}
