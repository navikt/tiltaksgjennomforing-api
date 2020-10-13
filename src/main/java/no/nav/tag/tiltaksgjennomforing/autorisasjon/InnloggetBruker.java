package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public abstract class InnloggetBruker<T extends Identifikator> {
    private final T identifikator;

    public abstract Avtalepart<T> avtalepart(Avtale avtale);

    public abstract boolean harLeseTilgang(Avtale avtale);

    public void sjekkLeseTilgang(Avtale avtale) {
        if (!harLeseTilgang(avtale)) {
            throw new TilgangskontrollException("Har ikke tilgang til avtalen.");
        }
    }

    public abstract boolean harSkriveTilgang(Avtale avtale);

    public void sjekkSkriveTilgang(Avtale avtale) {
        if (!harSkriveTilgang(avtale)) {
            throw new TilgangskontrollException("Har ikke tilgang til avtalen.");
        }
    }

    public List<Identifikator> identifikatorer() {
        return List.of(identifikator);
    }

    @JsonProperty
    public abstract boolean erNavAnsatt();

    @JsonProperty
    public abstract Avtalerolle rolle();

    public List<Avtale> hentAlleAvtalerMedLesetilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return hentAlleAvtalerMedMuligTilgang(avtaleRepository, queryParametre).stream()
                .filter(queryParametre)
                .filter(this::harLeseTilgang)
                .sorted(Comparator.nullsLast(Comparator.comparing(Avtale::getSistEndret).reversed()))
                .collect(Collectors.toList());
    }

    abstract List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre);

    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        return avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
    }
}
