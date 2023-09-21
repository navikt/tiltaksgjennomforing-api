package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterSokRepository extends JpaRepository<FilterSok, String> {

    FilterSok findFilterSokBySokId(String sokId);
}
