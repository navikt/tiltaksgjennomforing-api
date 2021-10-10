package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ArbeidsgiverNotifikasjonRepository extends JpaRepository<ArbeidsgiverNotifikasjon, UUID> {

    @Query(value = "FROM ArbeidsgiverNotifikasjon n WHERE n.avtaleId = (?1) AND n.hendelseUtfort = false")
    List<ArbeidsgiverNotifikasjon> aktiveNotifikasjoner(UUID id);
}
