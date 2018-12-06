package no.nav.tag.tiltaksgjennomforing;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaalRepository extends CrudRepository<Maal, Integer> {

    @Query("SELECT * FROM maal WHERE avtale = :avtaleId")
    List<Maal> hentMaalForAvtale(@Param("avtaleId") Integer avtaleId);
}
