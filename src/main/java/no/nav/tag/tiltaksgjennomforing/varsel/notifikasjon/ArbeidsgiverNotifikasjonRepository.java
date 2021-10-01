package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArbeidsgiverNotifikasjonRepository extends JpaRepository<Notifikasjon, UUID> {}
