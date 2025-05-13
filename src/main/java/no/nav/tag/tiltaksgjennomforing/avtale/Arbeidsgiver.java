package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Arbeidsgiver extends Avtalepart<Fnr> {
    private final Map<BedriftNr, Collection<Tiltakstype>> tilganger;
    private final Set<AltinnReportee> altinnOrganisasjoner;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final EregService eregService;

    public Arbeidsgiver(
            Fnr identifikator,
            Set<AltinnReportee> altinnOrganisasjoner,
            Map<BedriftNr, Collection<Tiltakstype>> tilganger,
            PersondataService persondataService,
            Norg2Client norg2Client,
            EregService eregService
    ) {
        super(identifikator);
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        this.tilganger = tilganger;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.eregService = eregService;
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret()
            .plus(84, ChronoUnit.DAYS)
            .isBefore(Now.instant());
    }

    private static boolean annullertForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.getAnnullertTidspunkt() != null && avtale.getAnnullertTidspunkt()
            .plus(84, ChronoUnit.DAYS)
            .isBefore(Now.instant());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getGjeldendeInnhold()
            .getSluttDato().plusWeeks(12)
            .isBefore(Now.localDate());
    }

    private static Avtale fjernAvbruttGrunn(Avtale avtale) {
        avtale.setAvbruttGrunn(null);
        return avtale;
    }

    private static Avtale fjernAnnullertGrunn(Avtale avtale) {
        avtale.setAnnullertGrunn(null);
        return avtale;
    }

    private static Avtale fjernKvalifiseringsgruppe(Avtale avtale) {
        avtale.setKvalifiseringsgruppe(null);
        avtale.setFormidlingsgruppe(null);
        return avtale;
    }

    @Override
    protected void avvisDatoerTilbakeITid(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (!avtale.erUfordelt()) {
            return;
        }
        if (startDato != null && startDato.isBefore(Now.localDate())) {
            throw new VarighetDatoErTilbakeITidException();
        }
        if (sluttDato != null && sluttDato.isBefore(Now.localDate())) {
            throw new VarighetDatoErTilbakeITidException();
        }
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
    public InnloggetBruker innloggetBruker() {
        return new InnloggetArbeidsgiver(getIdentifikator(), altinnOrganisasjoner, tilganger);
    }

    @Override
    public Collection<BedriftNr> identifikatorer() {
        return tilganger.keySet();
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        if (sluttdatoPassertMedMerEnn12Uker(avtale)) {
            return new Tilgang.Avvis(
                Avslagskode.SLUTTDATO_PASSERT,
                "Sluttdato har passert med mer enn 12 uker"
            );
        }
        if (avbruttForMerEnn12UkerSiden(avtale)) {
            return new Tilgang.Avvis(
                Avslagskode.UTGATT,
                "Avbrutt for mer enn 12 uker siden"
            );
        }
        if (annullertForMerEnn12UkerSiden(avtale)) {
            return new Tilgang.Avvis(
                Avslagskode.UTGATT,
                "Annullert for mer enn 12 uker siden"
            );
        }
        if (!harTilgangPåTiltakIBedrift(avtale.getBedriftNr(), avtale.getTiltakstype())) {
            return new Tilgang.Avvis(
                Avslagskode.IKKE_TILGANG_PAA_TILTAK,
                "Ikke tilgang på tiltak i valgt bedrift"
            );
        }
        return new Tilgang.Tillat();
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return avtale -> harTilgangTilAvtale(avtale).erTillat();
    }

    @Override
    public boolean avtalenEksisterer(Avtale avtale) {
        if (avtale.getOpphav().equals(Avtaleopphav.ARENA) && !avtale.erAvtaleInngått()) {
            return false;
        }
        return super.avtalenEksisterer(avtale);
    }

    private boolean harTilgangPåTiltakIBedrift(BedriftNr bedriftNr, Tiltakstype tiltakstype) {
        if (!tilganger.containsKey(bedriftNr)) {
            return false;
        }
        Collection<Tiltakstype> gyldigeTilgangerPåBedriftNr = tilganger.get(bedriftNr);
        return gyldigeTilgangerPåBedriftNr.contains(tiltakstype);
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        if (tilganger.isEmpty()) {
            return Page.empty();
        }
        Page<Avtale> avtaler;
        if (queryParametre.getTiltakstype() != null) {
            if (harTilgangPåTiltakIBedrift(queryParametre.getBedriftNr(), queryParametre.getTiltakstype()))
                avtaler = avtaleRepository.findAllByBedriftNrInAndTiltakstypeAndFeilregistrertIsFalse(Set.of(queryParametre.getBedriftNr()), queryParametre.getTiltakstype(), pageable);
            else if (queryParametre.getBedriftNr() == null) {
                avtaler = avtaleRepository.findAllByBedriftNrInAndTiltakstypeAndFeilregistrertIsFalse(tilganger.keySet(), queryParametre.getTiltakstype(), pageable);
            } else { // Bruker ba om informasjon på en bedrift hen ikke har tilgang til, og får dermed tom liste
                avtaler = Page.empty();
            }
        } else {
            if (queryParametre.getBedriftNr() != null && tilganger.containsKey(queryParametre.getBedriftNr()))
                avtaler = avtaleRepository.findAllByBedriftNrInAndFeilregistrertIsFalse(Set.of(queryParametre.getBedriftNr()), pageable);
            else if (queryParametre.getBedriftNr() == null) {
                avtaler = avtaleRepository.findAllByBedriftNrInAndFeilregistrertIsFalse(tilganger.keySet(), pageable);
            } else { // Bruker ba om informasjon på en bedrift hen ikke har tilgang til, og får dermed tom liste
                avtaler = Page.empty();
            }
        }
        return avtaler;
    }

    public List<Avtale> hentAvtalerForMinsideArbeidsgiver(AvtaleRepository avtaleRepository, BedriftNr bedriftNr) {
        long start = System.currentTimeMillis();

        List<Avtale> liste = avtaleRepository.findAllByBedriftNrAndFeilregistrertIsFalse(bedriftNr).stream()
                .filter(this::avtalenEksisterer)
                .filter(avtale -> this.harTilgangTilAvtale(avtale).erTillat())
                .map(Arbeidsgiver::fjernAvbruttGrunn)
                .map(Arbeidsgiver::fjernAnnullertGrunn)
                .collect(Collectors.toList());

        if (System.currentTimeMillis() - start > 10_000) {
            log.warn("Det tok mer enn 10 sek å hente {} avtaler for bedrift {}", liste.size(), bedriftNr.asString());
        }

        return liste;
    }

    private void tilgangTilBedriftVedOpprettelseAvAvtale(BedriftNr bedriftNr, Tiltakstype tiltakstype) {
        if (!harTilgangPåTiltakIBedrift(bedriftNr, tiltakstype)) {
            throw new TilgangskontrollException("Har ikke tilgang på tiltak i valgt bedrift");
        }
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        this.tilgangTilBedriftVedOpprettelseAvAvtale(opprettAvtale.getBedriftNr(), opprettAvtale.getTiltakstype());

        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.ARBEIDSGIVER);
        avtale.leggTilDeltakerNavn(persondataService.hentNavn(avtale.getDeltakerFnr()));
        sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        leggEnheterVedOpprettelseAvAvtale(avtale);

        return avtale;
    }

    private void leggEnheterVedOpprettelseAvAvtale(Avtale avtale) {
        super.hentGeoEnhetFraNorg2(avtale, norg2Client, persondataService);
        super.hentOppfølgingsenhetNavnFraNorg2(avtale, norg2Client);
    }

    private void sjekkOmBedriftErGyldigOgOppdaterNavn(Avtale avtale) {
        Optional.ofNullable(eregService.hentVirksomhet(avtale.getBedriftNr()))
            .map(Organisasjon::getBedriftNavn)
            .ifPresent(avtale::leggTilBedriftNavn);
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository, avtaleId);
        fjernAvbruttGrunn(avtale);
        fjernAnnullertGrunn(avtale);
        fjernKvalifiseringsgruppe(avtale);
        return avtale;
    }

}
