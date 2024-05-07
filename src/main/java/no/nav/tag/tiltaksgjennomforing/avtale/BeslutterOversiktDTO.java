package no.nav.tag.tiltaksgjennomforing.avtale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AvtaleMedFnrOgBedriftNr;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BeslutterOversiktDTO extends AvtaleMedFnrOgBedriftNr {
    String getId();
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
    LocalDateTime getSistEndret();

    @JsonIgnore
    @Override
    default FnrOgBedrift getFnrOgBedrift() {
        return new FnrOgBedrift(getDeltakerFnr(), getBedriftNr());
    }
}
