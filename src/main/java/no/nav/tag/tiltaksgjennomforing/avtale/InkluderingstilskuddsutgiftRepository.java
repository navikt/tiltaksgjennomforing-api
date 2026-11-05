package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

public interface InkluderingstilskuddsutgiftRepository extends JpaRepository<Inkluderingstilskuddsutgift, UUID> {

    /**
     * Sletter utgiftene med gitte id-er, men bare hvis de ikke lenger er koblet til noe avtaleinnhold. Utgifter som
     * deles med andre versjoner av avtaleinnholdet blir derfor ikke slettet.
     */
    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("""
        DELETE FROM Inkluderingstilskuddsutgift u
        WHERE u.id IN :ider
          AND NOT EXISTS (
              SELECT ai.id FROM AvtaleInnhold ai JOIN ai.inkluderingstilskuddsutgift x WHERE x.id = u.id
          )
        """)
    int slettUtgifterUtenKobling(@Param("ider") Collection<UUID> ider);
}
