package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class PdlRespons {
    private final Data data;

    public Optional<String> utledGeoLokasjon() {
        try {
            return Optional.of(getData().getHentGeografiskTilknytning().getGeoTilknytning());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Optional<String> utledGjeldendeIdent() {
        try {
            return Stream.of(getData().getHentIdenter().getIdenter())
                .filter(i -> !i.isHistorisk())
                .map(Identer::getIdent)
                .findFirst();
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Navn utledNavnEllerTomtNavn() {
        return utledNavn().orElse(Navn.TOMT_NAVN);
    }

    public Optional<Navn> utledNavn() {
        try {
            return Optional.of(getData().getHentPerson().getNavn()[0]);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Diskresjonskode utledDiskresjonskodeEllerUgradert() {
        return utledDiskresjonskode().orElse(Diskresjonskode.UGRADERT);
    }

    public Optional<Diskresjonskode> utledDiskresjonskode() {
        return utledAdressebeskyttelse().map(Adressebeskyttelse::getGradering);
    }

    public Map<Fnr, Optional<Diskresjonskode>> utledDiskresjonskode(Set<Fnr> fnrSet) {
        Set<String> fnrStringSet = fnrSet.stream().map(Fnr::asString).collect(Collectors.toSet());
        return Stream.of(getData().getHentPersonBolk().getHentPerson())
            .map(person -> {
                String folkeregisteridentifikator = Stream.of(person.getFolkeregisteridentifikator())
                    .map(Folkeregisteridentifikator::getIdentifikasjonsnummer)
                    .filter(fnrStringSet::contains)
                    .findFirst()
                    .orElseThrow();

                return Map.entry(
                    new Fnr(folkeregisteridentifikator),
                    utledAdressebeskyttelse(person).map(Adressebeskyttelse::getGradering)
                );
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Optional<Adressebeskyttelse> utledAdressebeskyttelse() {
        return Optional.ofNullable(this.getData())
            .flatMap(data -> utledAdressebeskyttelse(data.getHentPerson()));
    }

    private static Optional<Adressebeskyttelse> utledAdressebeskyttelse(HentPerson hentPerson) {
        try {
            return Optional.of(hentPerson.getAdressebeskyttelse()[0]);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

}
