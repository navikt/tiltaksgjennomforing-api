package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InnloggetDeltaker extends InnloggetBruker<Fnr> {

    public InnloggetDeltaker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public Deltaker avtalepart(Avtale avtale) {
        if (avtale.getDeltakerFnr().equals(getIdentifikator())) {
            return new Deltaker(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }

    @Override
    public boolean harLeseTilgang(Avtale avtale) {
        return avtalepart(avtale) != null;
    }

    @Override
    public boolean harSkriveTilgang(Avtale avtale) {
        return avtalepart(avtale) != null;
    }

    @Override
    public List<Identifikator> identifikatorer() {
        return List.of(getIdentifikator());
    }

    @Override
    public boolean erNavAnsatt() {
        return false;
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.DELTAKER;
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return avtaleRepository.findAllByDeltakerFnr(getIdentifikator());
    }
}
