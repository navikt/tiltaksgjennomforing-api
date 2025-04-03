package no.nav.tag.tiltaksgjennomforing.avtale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LangvarigLonnstilskuddRepository extends JpaRepository<LangvarigLonnstilskudd, UUID>, JpaSpecificationExecutor {
    @Override
    Optional<LangvarigLonnstilskudd> findById(UUID id);

    Optional<LangvarigLonnstilskudd> findByAvtale(Avtale avtale);
}
