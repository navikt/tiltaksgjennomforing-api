package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import no.nav.tag.tiltaksgjennomforing.infrastruktur.FnrOgBedrift;

import java.time.Instant;
import java.util.UUID;

/**
 * Definerer kontrakten som trengs for å auditlogge oppslaget på
 * en entitet. For at auditlogging skal fungere må vi vite om:
 * <p>
 * - id til entiteten<br/>
 * - sistEndret-tidspunkt<br/>
 * - fødselsnummer og bedriftsnummer
 * <p>
 * id og sistEndret-tidspunkt brukes for å identifisere en unik enhet
 * som det gjøres oppslag på (flere oppslag på samme data skal ignoreres)
 * <p>
 * fnr brukes for å finne ut hvem det ble gjort oppslag på
 * <p>
 * bedrift brukes dersom oppslag utføres av arbeidsgiver; auditlogging gjort
 * av arbeidsgivere skal ikke logges som et oppslag fra deres fødselsnummer,
 * men skal heller sies å ha blitt gjort av bedriften de tilhører
 */
public interface AuditerbarEntitet {
    UUID getId();
    Instant getSistEndret();
    FnrOgBedrift getFnrOgBedrift();
}
