package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditerbarAvtale;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BeslutterOversiktDTO extends AuditerbarAvtale {
    String getId();
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
    LocalDateTime getSistEndret();

    @Override
    default FnrOgBedrift getFnrOgBedrift() {
        return new FnrOgBedrift(getDeltakerFnr(), getBedriftNr());
    }
}
