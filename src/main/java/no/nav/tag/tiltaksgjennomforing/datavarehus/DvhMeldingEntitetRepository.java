package no.nav.tag.tiltaksgjennomforing.datavarehus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface DvhMeldingEntitetRepository extends JpaRepository<DvhMeldingEntitet, UUID> {}
