package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import java.util.List;
import java.util.Optional;

record KontorTilhorighetGraphqlRespons(Data data, List<GraphqlFeil> errors) {
    record Data(Oppfolgingskontor kontorTilhorighet) {}

    record GraphqlFeil(String message) {}

    boolean harFeil() {
        return errors != null && !errors.isEmpty();
    }

    Optional<Oppfolgingskontor> kontor() {
        return Optional.ofNullable(data).map(Data::kontorTilhorighet);
    }
}
