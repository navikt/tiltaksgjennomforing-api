package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BjelleVarselRepository extends CrudRepository<BjelleVarsel, UUID> {
    @Override
    List<BjelleVarsel> findAll();
    //    @Query("select b.* from bjelle_varsel b where b.identifikator = :identifikator)")
//    List<BjelleVarsel> finnAlleForIdentifikator(@Param("identifikator") Identifikator identfikator);
}
