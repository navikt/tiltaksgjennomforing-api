package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.Navn;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersondataService {
    private final PersondataClient persondataClient;

    public PersondataService(PersondataClient persondataClient) {
        this.persondataClient = persondataClient;
    }

    public Diskresjonskode hentDiskresjonskode(Fnr fnr) {
        return persondataClient.hentPersondata(fnr).utledDiskresjonskodeEllerUgradert();
    }

    public Map<Fnr, Diskresjonskode> hentDiskresjonskoder(Set<Fnr> fnrSet) {
        if (fnrSet.size() > 1000) {
            throw new IllegalArgumentException("Kan ikke hente diskresjonkode for mer enn 1000 om gangen");
        }

        return persondataClient.hentPersonBolk(fnrSet)
            .utledDiskresjonskoder(fnrSet)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(Diskresjonskode.UGRADERT)));
    }

    public Optional<AktorId> hentGjeldendeAktørId(Fnr fnr) {
        return persondataClient.hentPersondata(fnr)
            .utledGjeldendeIdent()
            .map(AktorId::av);
    }

    public Optional<String> hentGeografiskTilknytning(Fnr fnr) {
        return persondataClient.hentPersondata(fnr).utledGeoLokasjon();
    }

    public Navn hentNavn(Fnr fnr) {
        return persondataClient.hentPersondata(fnr).utledNavnEllerTomtNavn();
    }

}
