package no.nav.tag.tiltaksgjennomforing;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OppgaveRepository extends CrudRepository<Oppgave, Integer> {

    @Query("SELECT * FROM oppgave WHERE avtale = :avtaleId")
    List<Oppgave> hentOppgaverForAvtale(@Param("avtaleId") Integer avtaleId);
}
