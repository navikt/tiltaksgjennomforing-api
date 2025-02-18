package no.nav.tag.tiltaksgjennomforing.persondata.aktorId;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AktorIdService {
    private final AktorIdRepository aktorIdRepository;

    public AktorIdService(AktorIdRepository aktorIdRepository) {
        this.aktorIdRepository = aktorIdRepository;
    }

    public Optional<AktorId> hentAktorId(Fnr fnr) {
        return aktorIdRepository.findById(fnr.asString()).map(entity -> AktorId.av(entity.getAktorId()));
    }

    public Map<Fnr, AktorId> hentAktorId(Set<Fnr> fnrSet) {
        return aktorIdRepository.findAllById(fnrSet.stream().map(Fnr::asString).toList())
            .stream()
            .collect(Collectors.toMap(
                entity -> new Fnr(entity.getPersonId()),
                entity -> AktorId.av(entity.getAktorId())
            ));
    }

    public void lagreAktorId(Fnr fnr, String aktorId) {
        aktorIdRepository.save(
            AktorIdEntity.builder()
                .aktorId(aktorId)
                .personId(fnr.asString())
                .build()
        );
    }
}
