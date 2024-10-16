package no.nav.tag.tiltaksgjennomforing.satser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

interface SatserRepository extends JpaRepository<Satser, Integer> {
    List<Satser> findAllBySatsType(String satsType);

    @Query("select distinct(satsType) from Satser")
    Set<String> finnSatsetyper();
}
