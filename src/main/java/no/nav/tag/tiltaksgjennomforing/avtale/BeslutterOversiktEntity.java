package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface BeslutterOversiktEntity {
    UUID getId();
    Integer getAvtaleNr();
    Tiltakstype getTiltakstype();
    NavIdent getVeilederNavIdent();
    String getDeltakerFornavn();
    String getDeltakerEtternavn();
    Fnr getDeltakerFnr();
    String getBedriftNavn();
    BedriftNr getBedriftNr();
    LocalDate getStartDato();
    LocalDate getSluttDato();
    String getStatus();
    String getAntallUbehandlet();
    LocalDateTime getOpprettetTidspunkt();
    Instant getSistEndret();
    String getEnhetOppfolging();
}
