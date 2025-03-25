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
    private final PersondataDiskresjonskodeCache diskresjonskodeCache;

    public PersondataService(PersondataClient persondataClient) {
        this.persondataClient = persondataClient;
        this.diskresjonskodeCache = new PersondataDiskresjonskodeCache();
    }

    public Diskresjonskode hentDiskresjonskode(Fnr fnr) {
        return diskresjonskodeCache.get(fnr).orElseGet(() -> {
            Optional<Diskresjonskode> diskresjonskodeOpt = persondataClient.hentPersondata(fnr).utledDiskresjonskode();
            diskresjonskodeCache.putIfPresent(fnr, diskresjonskodeOpt);
            return diskresjonskodeOpt.orElse(Diskresjonskode.UGRADERT);
        });
    }

    public Map<Fnr, Diskresjonskode> hentDiskresjonskoder(Set<Fnr> fnrSet) {
        if (fnrSet.size() > 1000) {
            throw new IllegalArgumentException("Kan ikke hente diskresjonkode for mer enn 1000 om gangen");
        }

        Map<Fnr, Diskresjonskode> diskresjonskoderFraCache = diskresjonskodeCache.getIfPresent(fnrSet);
        Set<Fnr> fnrSomIkkeFinnesICache = fnrSet.stream()
            .filter(fnr -> !diskresjonskoderFraCache.containsKey(fnr))
            .collect(Collectors.toSet());

        Map<Fnr, Optional<Diskresjonskode>> diskresjonskodeOptFraPdl = persondataClient
            .hentPersonBolk(fnrSomIkkeFinnesICache)
            .utledDiskresjonskoder(fnrSomIkkeFinnesICache);

        diskresjonskodeCache.putAllIfPresent(diskresjonskodeOptFraPdl);

        Map<Fnr, Diskresjonskode> diskresjonskodeFraPdl = diskresjonskodeOptFraPdl.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().orElse(Diskresjonskode.UGRADERT)
            ));

        return PersondataDiskresjonskodeCache.concat(diskresjonskoderFraCache, diskresjonskodeFraPdl);
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
