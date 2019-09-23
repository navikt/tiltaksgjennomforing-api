package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AvtaleRepository extends CrudRepository<Avtale, UUID> {
   /* @Query("SELECT * FROM AVTALE a WHERE a.BASE_AVTALE_ID=:base_avtale_id ")
    List<Avtale> fetchAvtaler(@Param("base_avtale_id") UUID baseAvtaleId);

    List<Avtale> findAllByBaseAvtaleIdAndGodkjentAvVeileder(UUID baseAvtaleId, LocalDateTime godkjentAvVeileder);
    List<Avtale> findAllByBaseAvtaleId(UUID baseAvtaleId);
    List<Avtale> findAllByVersjon(int versjon);
*/
}
