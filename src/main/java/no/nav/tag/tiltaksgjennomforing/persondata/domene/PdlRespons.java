package no.nav.tag.tiltaksgjennomforing.persondata.domene;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;

import java.util.NoSuchElementException;
import java.util.Optional;

public record PdlRespons(Data data) {
    public record Data(
        HentPerson hentPerson,
        HentIdenter hentIdenter,
        HentGeografiskTilknytning hentGeografiskTilknytning
    ) { }

    public Optional<String> utledGeoLokasjon() {
        try {
            return Optional.of(data().hentGeografiskTilknytning().getGeoTilknytning());
        } catch (NullPointerException | NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public Optional<String> utledGjeldendeIdent() {
        try {
            return data().hentIdenter().identer().stream()
                .filter(i -> !i.isHistorisk())
                .map(Identer::getIdent)
                .findFirst();
        } catch (NullPointerException | NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public Navn utledNavnEllerTomtNavn() {
        return utledNavn().orElse(Navn.TOMT_NAVN);
    }

    public Optional<Navn> utledNavn() {
        try {
            return Optional.of(data().hentPerson().navn().getFirst());
        } catch (NullPointerException | NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public Diskresjonskode utledDiskresjonskodeEllerUgradert() {
        return utledDiskresjonskode().orElse(Diskresjonskode.UGRADERT);
    }

    public Optional<Diskresjonskode> utledDiskresjonskode() {
        return Optional.ofNullable(data())
            .flatMap(data -> utledAdressebeskyttelse(data.hentPerson()))
            .map(Adressebeskyttelse::getGradering);
    }

    public static Optional<Adressebeskyttelse> utledAdressebeskyttelse(HentPerson hentPerson) {
        try {
            return Optional.of(hentPerson.adressebeskyttelse().getFirst());
        } catch (NullPointerException | NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
