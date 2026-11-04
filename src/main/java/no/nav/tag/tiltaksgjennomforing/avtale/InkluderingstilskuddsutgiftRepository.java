package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface InkluderingstilskuddsutgiftRepository extends JpaRepository<Inkluderingstilskuddsutgift, UUID> {

    /**
     * Sletter utgifter som ikke lenger er koblet til noe avtaleinnhold. Utgifter som deles med andre versjoner
     * av avtaleinnholdet har fortsatt en kobling, og blir derfor ikke slettet.
     */
    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("""
        DELETE FROM Inkluderingstilskuddsutgift u
        WHERE NOT EXISTS (
            SELECT ai.id FROM AvtaleInnhold ai JOIN ai.inkluderingstilskuddsutgift x WHERE x.id = u.id
        )
        """)
    int slettUtgifterUtenKobling();
}
