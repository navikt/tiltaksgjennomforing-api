package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VarslbarHendelseRepository extends CrudRepository<VarslbarHendelse, UUID> {

    @Query("select h.* from varslbar_hendelse h join sms_varsel s on h.id = s.varslbar_hendelse where s.id = :smsVarselId")
    Optional<VarslbarHendelse> finnForSmsVarselId(@Param("smsVarselId") UUID smsVarselId);

    @Query("select count(*) from sms_varsel s where s.status = 'USENDT'")
    long antallUsendteSmsVarsler();
}
