package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


public class Arbeidsgiver extends Avtalepart<Fnr> {
    static String tekstAvtaleVenterPaaDinGodkjenning = "Deltakeren trenger ikke å godkjenne avtalen før du gjør det. ";
    static String ekstraTekstAvtaleVenterPaaDinGodkjenning = "Veilederen skal godkjenne til slutt.";
    static String tekstTiltaketErAvsluttet = "Hvis du har spørsmål må du kontakte NAV.";
    private final Map<BedriftNr, Collection<Tiltakstype>> tilganger;
    private final Set<AltinnReportee> altinnOrganisasjoner;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;

    public Arbeidsgiver(Fnr identifikator, Set<AltinnReportee> altinnOrganisasjoner, Map<BedriftNr, Collection<Tiltakstype>> tilganger, PersondataService persondataService, Norg2Client norg2Client) {
        super(identifikator);
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        this.tilganger = tilganger;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret().plus(84, ChronoUnit.DAYS).isBefore(Instant.now());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getSluttDato().plusWeeks(12).isBefore(LocalDate.now());
    }

    private static Avtale fjernAvbruttGrunn(Avtale avtale) {
        avtale.setAvbruttGrunn(null);
        return avtale;
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForArbeidsgiver(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        avtaleStatusDetaljer.setGodkjentAvInnloggetBruker(erGodkjentAvInnloggetBruker(avtale));

        switch (avtale.statusSomEnum()) {
            case AVBRUTT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
                break;
            case PÅBEGYNT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, "", "");
                break;
            case MANGLER_GODKJENNING:
                if (avtale.erGodkjentAvArbeidsgiver())
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderVentAndreGodkjenning, "", "");
                else
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleVenterPaaDinGodkjenning,
                            Arbeidsgiver.tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
                break;
            case KLAR_FOR_OPPSTART:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter) + ".", "");
                break;
            case GJENNOMFØRES:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleGjennomfores, "", "");
                break;
            case AVSLUTTET:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErAvsluttet, tekstTiltaketErAvsluttet, "");
                break;
        }

        avtaleStatusDetaljer.setPart1Detaljer((avtale.getDeltakerFornavn() != null && !avtale.getDeltakerFornavn().trim().equals("") ? avtale.getDeltakerFornavn() : "Deltaker") + " " +
                (avtale.getDeltakerEtternavn() != null && !avtale.getDeltakerEtternavn().trim().equals("") ? avtale.getDeltakerEtternavn() + " " : "")
                + (avtale.erGodkjentAvDeltaker() ? "har godkjent" : "har ikke godkjent"), avtale.erGodkjentAvDeltaker());
        avtaleStatusDetaljer.setPart2Detaljer((avtale.getVeilederFornavn() != null && !avtale.getVeilederFornavn().trim().equals("") ? avtale.getVeilederFornavn() : "Veileder") + " "
                + (avtale.getVeilederEtternavn() != null && !avtale.getVeilederEtternavn().trim().equals("") ? avtale.getVeilederEtternavn() + " " : "")
                + (avtale.erGodkjentAvVeileder() ? "har godkjent" : "har ikke godkjent"), avtale.erGodkjentAvVeileder());
        return avtaleStatusDetaljer;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return avtale.erGodkjentAvArbeidsgiver();
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return !avtale.erGodkjentAvVeileder();
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        avtale.opphevGodkjenningerSomArbeidsgiver();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.ARBEIDSGIVER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {
        throw new TilgangskontrollException("Arbeidsgiver kan ikke låse opp avtale");
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetArbeidsgiver(getIdentifikator(), altinnOrganisasjoner, tilganger);
    }

    @Override
    public Collection<BedriftNr> identifikatorer() {
        return tilganger.keySet();
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        if (sluttdatoPassertMedMerEnn12Uker(avtale)) {
            return false;
        }
        if (avbruttForMerEnn12UkerSiden(avtale)) {
            return false;
        }
        return harTilgangPåTiltakIBedrift(avtale.getBedriftNr(), avtale.getTiltakstype());
    }

    private boolean harTilgangPåTiltakIBedrift(BedriftNr bedriftNr, Tiltakstype tiltakstype) {
        if (!tilganger.containsKey(bedriftNr)) {
            return false;
        }
        Collection<Tiltakstype> gyldigeTilgangerPåBedriftNr = tilganger.get(bedriftNr);
        return gyldigeTilgangerPåBedriftNr.contains(tiltakstype);
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (tilganger.isEmpty()) {
            return Collections.emptyList();
        }
        return avtaleRepository.findAllByBedriftNrIn(tilganger.keySet()).stream().map(Arbeidsgiver::fjernAvbruttGrunn).collect(Collectors.toList());
    }

    @Override
    public List<Avtale> hentAlleAvtalerMedLesetilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return super.hentAlleAvtalerMedLesetilgang(avtaleRepository, queryParametre).stream().map(Arbeidsgiver::fjernAvbruttGrunn).collect(Collectors.toList());
    }

    public List<Avtale> hentAvtalerForMinsideArbeidsgiver(AvtaleRepository avtaleRepository, BedriftNr bedriftNr) {
        return avtaleRepository.findAllByBedriftNr(bedriftNr).stream()
                .filter(this::harTilgang)
                .map(Arbeidsgiver::fjernAvbruttGrunn)
                .collect(Collectors.toList());
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        if (!harTilgangPåTiltakIBedrift(opprettAvtale.getBedriftNr(), opprettAvtale.getTiltakstype())) {
            throw new TilgangskontrollException("Har ikke tilgang på tiltak i valgt bedrift");
        }
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(opprettAvtale);
        final PdlRespons persondata = persondataService.hentPersondata(opprettAvtale.getDeltakerFnr());
        leggTilGeografiskEnhet(avtale, persondata, norg2Client);
        return avtale;
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository, avtaleId);
        return fjernAvbruttGrunn(avtale);
    }
}
