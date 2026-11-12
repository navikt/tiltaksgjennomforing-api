package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import java.util.List;
import java.util.Optional;

record OppfolgingsenhetGraphqlRespons(Data data, List<GraphqlFeil> errors) {
    record Data(OppfolgingsEnhetsInfo oppfolgingsEnhet) {}

    record OppfolgingsEnhetsInfo(Oppfolgingsenhet enhet) {}

    record GraphqlFeil(String message) {}

    boolean harFeil() {
        return errors != null && !errors.isEmpty();
    }

    Optional<Oppfolgingsenhet> enhet() {
        return Optional.ofNullable(data)
            .map(Data::oppfolgingsEnhet)
            .map(OppfolgingsEnhetsInfo::enhet);
    }
}
