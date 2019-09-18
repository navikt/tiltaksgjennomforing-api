package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {
    //@Query("SELECT * FROM Avtale a WHERE a.base_avtale_id")
    List<Avtale> findAllByBaseAvtaleIdAndGodkjentAvVeileder(UUID baseAvtaleId, LocalDateTime godkjentAvVeileder);
}
