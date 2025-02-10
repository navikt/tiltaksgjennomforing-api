package no.nav.tag.tiltaksgjennomforing.datadeling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AvtaleMeldingEntitetRepository extends JpaRepository<AvtaleMeldingEntitet, UUID> {
    List<AvtaleMeldingEntitet> findAllByAvtaleId(UUID avtaleId);

    @Query(value = """
        WITH siste_meldinger AS (
            SELECT DISTINCT ON (avtale_id) melding_id, avtale_id, tidspunkt
            FROM avtale_melding am
            ORDER BY avtale_id, tidspunkt DESC
        )
        SELECT distinct a.id
        FROM avtale a, avtale_innhold ai, avtale_melding am, siste_meldinger sm
        WHERE a.gjeldende_innhold_id = ai.id
          AND a.id = am.avtale_id
          AND am.melding_id = sm.melding_id
          AND ai.antall_dager_per_uke IS NOT NULL
          AND CAST(json AS jsonb) ->> 'antallDagerPerUke' IS NULL;
      """, nativeQuery = true)
    List<UUID> findAlleAvtalerFraAvtaleMeldingerSomManglerAntallDagerPerUke();
}
