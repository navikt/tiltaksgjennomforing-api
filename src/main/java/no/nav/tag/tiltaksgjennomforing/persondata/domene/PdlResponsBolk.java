package no.nav.tag.tiltaksgjennomforing.persondata.domene;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record PdlResponsBolk(Data data) {
    public record Data(List<HentPersonBolk> hentPersonBolk) {}

    public Map<Fnr, Optional<Diskresjonskode>> utledDiskresjonskoder(Set<Fnr> fnrSet) {
        List<HentPersonBolk> bolk = Optional.ofNullable(data())
            .flatMap(bolkData -> Optional.ofNullable(bolkData.hentPersonBolk()))
            .orElse(Collections.emptyList());
        Map<String, Optional<Diskresjonskode>> diskresjonskodeMap = bolk.stream()
            .filter(HentPersonBolk::isOk)
            .flatMap(person -> person.person().folkeregisteridentifikator().stream().map(a -> Map.entry(
                a.identifikasjonsnummer(),
                PdlRespons.utledAdressebeskyttelse(person.person()).map(Adressebeskyttelse::getGradering)
            )))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a.isEmpty() ? b : a
            ));

        return fnrSet.stream().collect(Collectors.toMap(
            fnr -> fnr,
            fnr -> diskresjonskodeMap.getOrDefault(fnr.asString(), Optional.empty())
        ));
    }
}
