package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import io.micrometer.core.annotation.Timed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ArbeidsgiverNotifikasjonRepository extends JpaRepository<ArbeidsgiverNotifikasjon, UUID> {

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query("FROM ArbeidsgiverNotifikasjon "
            + "where varselSendtVellykket = true and NotifikasjonIkkeLest = false")
    List<ArbeidsgiverNotifikasjon> findArbeidsgiverNotifikasjonByAvtaleId(UUID id);

    @Query(value = "FROM ArbeidsgiverNotifikasjon n WHERE n.avtaleId = (?1) AND n.varselSendtVellykket = false")
    List<ArbeidsgiverNotifikasjon> aktiveNotifikasjoner(UUID id);
}
