package no.nav.tag.tiltaksgjennomforing.hendelselogg;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HendelseloggRepository extends JpaRepository<Hendelselogg, UUID> {
    List<Hendelselogg> findAllByAvtaleId(UUID avtaleId);
}
