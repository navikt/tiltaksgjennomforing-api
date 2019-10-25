package no.nav.tag.tiltaksgjennomforing.varsel;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SmsVarselRepository extends CrudRepository<SmsVarsel, UUID> {
    @Query("select count(s) from SmsVarsel s where s.status = 'USENDT'")
    long antallUsendte();
}
