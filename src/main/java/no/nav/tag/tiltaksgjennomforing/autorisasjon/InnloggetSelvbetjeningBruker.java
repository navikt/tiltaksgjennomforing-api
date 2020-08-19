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

@Data
@EqualsAndHashCode(callSuper = true)
public class InnloggetSelvbetjeningBruker extends InnloggetBruker<Fnr> {

    private final List<ArbeidsgiverOrganisasjon> organisasjoner;

    public InnloggetSelvbetjeningBruker(Fnr identifikator, List<ArbeidsgiverOrganisasjon> organisasjoner) {
        super(identifikator);
        this.organisasjoner = organisasjoner != null ? organisasjoner : new ArrayList<ArbeidsgiverOrganisasjon>();
    }

    @Override
    public Avtalepart<Fnr> avtalepart(Avtale avtale) {
        if (avtale.getDeltakerFnr().equals(getIdentifikator())) {
            return new Deltaker(getIdentifikator(), avtale);
        } else if (sjekkOmArbeidsgiverHarTilgang(avtale)) {
            return new Arbeidsgiver(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }

    private boolean sjekkOmArbeidsgiverHarTilgang(Avtale avtale) {
        if (sluttdatoPassertMedMerEnn12Uker(avtale)) {
            return false;
        }
        if (avbruttForMerEnn12UkerSiden(avtale)) {
            return false;
        }
        // Sjekk om du har en ArbeidsgiverOrganisasjon som matcher både bedriftNr og tilgangstype på avtale
        return organisasjoner.stream().anyMatch(o -> o.getTilgangstyper().contains(avtale.getTiltakstype()) && o.getBedriftNr().equals(avtale.getBedriftNr()));
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret().plus(84, ChronoUnit.DAYS).isBefore(Instant.now());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getSluttDato().plusWeeks(12).isBefore(LocalDate.now());
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
        var identifikatorer = new ArrayList<Identifikator>();
        identifikatorer.addAll(super.identifikatorer());
        identifikatorer.addAll(arbeidsgiverIdentifikatorer());
        return identifikatorer;
    }

    @Override
    public boolean erNavAnsatt() {
        return false;
    }

    private List<BedriftNr> arbeidsgiverIdentifikatorer() {
        return organisasjoner.stream().map(ArbeidsgiverOrganisasjon::getBedriftNr).collect(Collectors.toList());
    }


}
