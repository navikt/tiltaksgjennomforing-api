package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InnloggetArbeidsgiver extends InnloggetBruker<Fnr> {
    private final List<ArbeidsgiverOrganisasjon> organisasjoner;
    private final Set<AltinnOrganisasjon> altinnOrganisasjoner;
    private final Map<BedriftNr, Set<Tiltakstype>> tilganger = new HashMap<>();

    public InnloggetArbeidsgiver(Fnr identifikator, List<ArbeidsgiverOrganisasjon> organisasjoner, Set<AltinnOrganisasjon> altinnOrganisasjoner) {
        super(identifikator);
        this.organisasjoner = organisasjoner;
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        organisasjoner.forEach(org -> tilganger.put(org.getBedriftNr(), new HashSet<>(org.getTilgangstyper())));
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret().plus(84, ChronoUnit.DAYS).isBefore(Instant.now());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getSluttDato().plusWeeks(12).isBefore(LocalDate.now());
    }

    public Map<BedriftNr, Set<Tiltakstype>> getTilganger() {
        return tilganger;
    }

    public List<ArbeidsgiverOrganisasjon> getOrganisasjoner() {
        return organisasjoner;
    }

    public Set<AltinnOrganisasjon> getAltinnOrganisasjoner() {
        return altinnOrganisasjoner;
    }

    @Override
    public Avtalepart<Fnr> avtalepart(Avtale avtale) {
        if (sjekkOmArbeidsgiverHarTilgang(avtale)) {
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

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.ARBEIDSGIVER;
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (organisasjoner.isEmpty()) {
            return Collections.emptyList();
        }
        return avtaleRepository.findAllByBedriftNrIn(arbeidsgiverIdentifikatorer());
    }

    private List<BedriftNr> arbeidsgiverIdentifikatorer() {
        return organisasjoner.stream().map(ArbeidsgiverOrganisasjon::getBedriftNr).collect(Collectors.toList());
    }

    public List<Avtale> hentAvtalerForMinsideArbeidsgiver(AvtaleRepository avtaleRepository, BedriftNr bedriftNr) {
        return avtaleRepository.findAllByBedriftNr(bedriftNr).stream()
                .filter(this::harLeseTilgang)
                .collect(Collectors.toList());
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.arbeidsgiverOppretterAvtale(opprettAvtale);
    }
}
