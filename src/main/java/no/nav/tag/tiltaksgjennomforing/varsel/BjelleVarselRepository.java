package no.nav.tag.tiltaksgjennomforing.varsel;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface BjelleVarselRepository extends CrudRepository<BjelleVarsel, UUID> {
    @Override
    List<BjelleVarsel> findAll();
    //    @Query("select b.* from bjelle_varsel b where b.identifikator = :identifikator)")
//    List<BjelleVarsel> finnAlleForIdentifikator(@Param("identifikator") Identifikator identfikator);
}
