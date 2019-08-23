package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BjelleVarselRepository extends CrudRepository<BjelleVarsel, UUID> {
    @Query("select * from bjelle_varsel where bjelle_varsel.identifikator = :identifikator)")
    List<BjelleVarsel> finnAlleForIdentifikator(@Param("identifikator") String identfikator);
}
