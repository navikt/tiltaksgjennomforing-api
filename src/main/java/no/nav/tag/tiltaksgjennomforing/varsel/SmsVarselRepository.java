package no.nav.tag.tiltaksgjennomforing.varsel;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SmsVarselRepository extends CrudRepository<SmsVarsel, UUID> {
    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    @Query("select count(s) from SmsVarsel s where s.status = 'USENDT'")
    long antallUsendte();
}
