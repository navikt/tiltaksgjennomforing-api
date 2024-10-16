package no.nav.tag.tiltaksgjennomforing.satser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

interface SatserRepository extends JpaRepository<SatserEntitet, Integer> {
    List<SatserEntitet> findAllBySatsType(String satsType);

    @Query("select distinct(satsType) from satser")
    Set<String> finnSatsetyper();
}
