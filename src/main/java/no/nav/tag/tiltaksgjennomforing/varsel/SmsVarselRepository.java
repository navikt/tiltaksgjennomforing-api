package no.nav.tag.tiltaksgjennomforing.varsel;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SmsVarselRepository extends CrudRepository<SmsVarsel, UUID> {

    @Query("select count(*) from sms_varsel s where s.status = 'USENDT'")
    long antallUsendte();
}
