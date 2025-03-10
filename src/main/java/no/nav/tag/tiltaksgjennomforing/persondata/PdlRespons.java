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
        Map<String, Optional<Diskresjonskode>> diskresjonskodeMap = Stream.of(getData().getHentPersonBolk())
            .filter(HentPersonBolk::isOk)
            .flatMap(person -> Stream.of(person.getPerson().getFolkeregisteridentifikator()).map(a -> Map.entry(
                a.getIdentifikasjonsnummer(),
                utledAdressebeskyttelse(person.getPerson()).map(Adressebeskyttelse::getGradering)
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
