package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InnloggetArbeidsgiver extends InnloggetBruker<Fnr> {
    private final Set<AltinnReportee> altinnOrganisasjoner;
    private final Map<BedriftNr, Collection<Tiltakstype>> tilganger;

    public InnloggetArbeidsgiver(Fnr identifikator, Map<BedriftNr, Collection<Tiltakstype>> tilganger, Set<AltinnReportee> altinnOrganisasjoner) {
        super(identifikator);
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        this.tilganger = tilganger;
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret().plus(84, ChronoUnit.DAYS).isBefore(Instant.now());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getSluttDato().plusWeeks(12).isBefore(LocalDate.now());
    }

    public Map<BedriftNr, Collection<Tiltakstype>> getTilganger() {
        return tilganger;
    }

    public Set<AltinnReportee> getAltinnOrganisasjoner() {
        return altinnOrganisasjoner;
    }

    @Override
    public Avtalepart<Fnr> avtalepart(Avtale avtale) {
        if (sjekkOmArbeidsgiverHarTilgang(avtale)) {
            return new Arbeidsgiver(getIdentifikator());
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
        if (!tilganger.containsKey(avtale.getBedriftNr())) {
            return false;
        }
        Collection<Tiltakstype> gyldigeTilgangerPåBedriftNr = tilganger.get(avtale.getBedriftNr());
        return gyldigeTilgangerPåBedriftNr.contains(avtale.getTiltakstype());
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
        identifikatorer.addAll(tilganger.keySet());
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
        if (tilganger.isEmpty()) {
            return Collections.emptyList();
        }
        return avtaleRepository.findAllByBedriftNrIn(tilganger.keySet()).stream().map(this::fjernAvbruttGrunn).collect(Collectors.toList());
    }

    @Override
    public List<Avtale> hentAlleAvtalerMedLesetilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return super.hentAlleAvtalerMedLesetilgang(avtaleRepository, queryParametre).stream().map(this::fjernAvbruttGrunn).collect(Collectors.toList());
    }

    public List<Avtale> hentAvtalerForMinsideArbeidsgiver(AvtaleRepository avtaleRepository, BedriftNr bedriftNr) {
        return avtaleRepository.findAllByBedriftNr(bedriftNr).stream()
                .filter(this::harLeseTilgang)
                .map(this::fjernAvbruttGrunn)
                .collect(Collectors.toList());
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.arbeidsgiverOppretterAvtale(opprettAvtale);
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        return avtaleRepository.findById(avtaleId).map(this::fjernAvbruttGrunn).orElseThrow(RessursFinnesIkkeException::new);
    }

    private Avtale fjernAvbruttGrunn(Avtale avtale) {
        avtale.setAvbruttGrunn(null);
        return avtale;
    }

}
