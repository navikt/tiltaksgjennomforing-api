package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface BeslutterOversiktDTO {
    UUID getId();
    Integer getAvtaleNr();
    Tiltakstype getTiltakstype();
    NavIdent getVeilederNavIdent();
    String getDeltakerFornavn();
    String getDeltakerEtternavn();
    @JsonIgnore
    Fnr getDeltakerFnr();
    String getBedriftNavn();
    @JsonIgnore
    BedriftNr getBedriftNr();
    LocalDate getStartDato();
    LocalDate getSluttDato();
    String getStatus();
    String getAntallUbehandlet();
    LocalDateTime getOpprettetTidspunkt();
    Instant getSistEndret();
}
