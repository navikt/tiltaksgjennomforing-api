package no.nav.tag.tiltaksgjennomforing.datadeling;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvtaleMeldingEntitetRepository extends JpaRepository<AvtaleMeldingEntitet, UUID> {
    List<AvtaleMeldingEntitet> findAllByAvtaleId(UUID avtaleId);
    List<AvtaleMeldingEntitet> findAllByTidspunktAfterAndHendelse();
}
