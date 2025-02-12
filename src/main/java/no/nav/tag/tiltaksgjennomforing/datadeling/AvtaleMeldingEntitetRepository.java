package no.nav.tag.tiltaksgjennomforing.datadeling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AvtaleMeldingEntitetRepository extends JpaRepository<AvtaleMeldingEntitet, UUID> {
    List<AvtaleMeldingEntitet> findAllByAvtaleId(UUID avtaleId);

    @Query(value = """
        SELECT distinct avtale_id
        FROM avtale a, avtale_innhold ai, arena_agreement_migration aam, arena_tiltakdeltaker atd
        WHERE a.gjeldende_innhold_id = ai.id
          AND aam.avtale_id = a.id
          AND aam.tiltakdeltaker_id = atd.tiltakdeltaker_id
          AND ai.innhold_type = 'ENDRET_AV_ARENA'
          AND atd.dato_til = '2025-01-24'
          AND ai.slutt_dato = '2025-01-23'
      """, nativeQuery = true)
    List<UUID> finaAlleAvtalerSomHarFeilDatoFraMigrering();
}
