package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersondataService {
    private final PersondataClient persondataKlient;

    public PersondataService(PersondataClient persondataKlient) {
        this.persondataKlient = persondataKlient;
    }

    public Diskresjonskode hentDiskresjonskode(Fnr fnr) {
        return persondataKlient.hentPersondata(fnr).utledDiskresjonskodeEllerUgradert();
    }

    public Map<Fnr, Diskresjonskode> hentDiskresjonskoder(Set<Fnr> fnr) {
        if (fnr.size() > 1000) {
            throw new IllegalArgumentException("Kan ikke hente diskresjonkode for mer enn 1000 om gangen");
        }

        return persondataKlient.hentPersonBolk(fnr)
            .utledDiskresjonskode(fnr)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(Diskresjonskode.UGRADERT)));
    }

    public Optional<AktorId> hentGjeldendeAktørId(Fnr fnr) {
        return persondataKlient.hentPersondata(fnr)
            .utledGjeldendeIdent()
            .map(AktorId::av);
    }

    public Optional<String> hentGeografiskTilknytning(Fnr fnr) {
        return persondataKlient.hentPersondata(fnr).utledGeoLokasjon();
    }

    public Navn hentNavn(Fnr fnr) {
        return persondataKlient.hentPersondata(fnr).utledNavnEllerTomtNavn();
    }

}
