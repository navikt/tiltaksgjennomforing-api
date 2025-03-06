package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersondataService {
    private final PersondataKlient persondataKlient;

    public PersondataService(PersondataKlient persondataKlient) {
        this.persondataKlient = persondataKlient;
    }

    public Diskresjonskode hentDiskresjonskode(Fnr fnr) {
        return persondataKlient.hentAdressebeskyttelse(fnr).utledDiskresjonskodeEllerUgradert();
    }

    public Map<Fnr, Diskresjonskode> hentDiskresjonskode(Set<Fnr> fnr) {
        if (fnr.size() > 1000) {
            throw new IllegalArgumentException("Kan ikke hente diskresjonkode for mer enn 1000 om gangen");
        }

        return persondataKlient.hentAdressebeskyttelse(fnr)
            .utledDiskresjonskode(fnr)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(Diskresjonskode.UGRADERT)));
    }

    public Optional<AktorId> hentGjeldendeAktørId(Fnr fnr) {
        return Optional.ofNullable(fnr)
            .map(f -> persondataKlient.hentIdenter(fnr))
            .flatMap(pdlRespons -> pdlRespons.utledGjeldendeIdent().map(AktorId::av));
    }

    @Cacheable(CacheConfig.PDL_CACHE)
    public PdlRespons hentPersondata(Fnr fnr) {
        return persondataKlient.hentPersondata(fnr);
    }
}
