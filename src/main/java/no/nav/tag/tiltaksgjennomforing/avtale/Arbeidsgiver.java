package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
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
    private final List<BedriftNr> adressesperreTilgang;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final EregService eregService;

    public Arbeidsgiver(
            Fnr identifikator,
            Set<AltinnReportee> altinnOrganisasjoner,
            Map<BedriftNr, Collection<Tiltakstype>> tilganger,
            List<BedriftNr> adressesperreTilgang,
            PersondataService persondataService,
            Norg2Client norg2Client,
            EregService eregService
    ) {
        super(identifikator);
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        this.tilganger = tilganger;
        this.adressesperreTilgang = adressesperreTilgang;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.eregService = eregService;
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
        return tilgangTilAvtaler(List.of(avtale)).get(avtale);
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        Map<Avtale, Tilgang> tilgangsmappe = tilgangTilAvtaler(avtaler);
        return avtale -> tilgangsmappe.get(avtale).erTillat();
    }

    private Map<Avtale, Tilgang> tilgangTilAvtaler(List<Avtale> avtaler) {
        // Arbeidsgiver henter alltid en eller flere avtaler på samme bedriftNr, derav anyMatch.
        boolean harAdressesperretilgang = avtaler.stream().anyMatch(avtale -> adressesperreTilgang.contains(avtale.getBedriftNr()));
        Set<Fnr> unikeFnrIAvtaler = avtaler.stream().map(Avtale::getDeltakerFnr).collect(Collectors.toSet());
        // Slå opp PDL i bolk hvis man ikke har adressesperretilgang
        Map<Fnr, Diskresjonskode> diskresjonskodeMap = !harAdressesperretilgang ? persondataService.hentDiskresjonskoder(unikeFnrIAvtaler) : Collections.emptyMap();

        Map<Avtale, Tilgang> tilgangsmappe = avtaler.stream()
            .collect(Collectors.toMap(avtale -> avtale, avtale -> {
                boolean deltakerHarAdressesperre = diskresjonskodeMap.getOrDefault(avtale.getDeltakerFnr(), Diskresjonskode.UGRADERT).erKode6Eller7();
                if (!harAdressesperretilgang && deltakerHarAdressesperre) {
                    return new Tilgang.Avvis(
                        Avslagskode.IKKE_TILGANG_TIL_DELTAKER,
                        "Deltaker har adressesperre og arbeidsgiver har ikke adressesperretilgang i bedrift"
                    );
                }

                if (avtale.harSluttdatoPassertMedMerEnn12Uker()) {
                    return new Tilgang.Avvis(
                        Avslagskode.SLUTTDATO_PASSERT,
                        "Sluttdato har passert med mer enn 12 uker"
                    );
                }
                if (avtale.erAvbruttForMerEnn12UkerSiden()) {
                    return new Tilgang.Avvis(
                        Avslagskode.UTGATT,
                        "Avbrutt for mer enn 12 uker siden"
                    );
                }
                if (avtale.erAnnullertForMerEnn12UkerSiden()) {
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
            }));
        return tilgangsmappe;
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

    private boolean harTilgangPåDeltakerIBedrift(BedriftNr bedriftNr, Fnr deltakerFnr) {
        if (!adressesperreTilgang.contains(bedriftNr)) {
            Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(deltakerFnr);
            if (diskresjonskode.erKode6Eller7()) {
                return false;
            }
        }
        return true;
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        final LocalDate dato12UkerTilbakeFraNå = Now.localDate().minusWeeks(12);
        if (tilganger.isEmpty()) {
            return Page.empty();
        }
        Page<Avtale> avtaler;
        if (queryParametre.getTiltakstype() != null) {
            if (harTilgangPåTiltakIBedrift(queryParametre.getBedriftNr(), queryParametre.getTiltakstype())) {
                avtaler = avtaleRepository.findAllByBedriftNrInAndTiltakstype(
                    Set.of(queryParametre.getBedriftNr()),
                    queryParametre.getTiltakstype(),
                    dato12UkerTilbakeFraNå,
                    pageable
                );
            } else if (queryParametre.getBedriftNr() == null) {
                avtaler = avtaleRepository.findAllByBedriftNrInAndTiltakstype(
                    tilganger.keySet(),
                    queryParametre.getTiltakstype(),
                    dato12UkerTilbakeFraNå,
                    pageable
                );
            } else { // Bruker ba om informasjon på en bedrift hen ikke har tilgang til, og får dermed tom liste
                avtaler = Page.empty();
            }
        } else {
            if (queryParametre.getBedriftNr() != null && tilganger.containsKey(queryParametre.getBedriftNr())) {
                avtaler = avtaleRepository.findAllByBedriftNr(
                    Set.of(queryParametre.getBedriftNr()),
                    dato12UkerTilbakeFraNå,
                    pageable
                );
            } else if (queryParametre.getBedriftNr() == null) {
                avtaler = avtaleRepository.findAllByBedriftNr(tilganger.keySet(), dato12UkerTilbakeFraNå, pageable);
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

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        if (!harTilgangPåTiltakIBedrift(opprettAvtale.getBedriftNr(), opprettAvtale.getTiltakstype())) {
            throw new TilgangskontrollException("Har ikke tilgang på tiltak i valgt bedrift");
        }
        if (!harTilgangPåDeltakerIBedrift(opprettAvtale.getBedriftNr(), opprettAvtale.getDeltakerFnr())) {
            throw new IkkeTilgangTilDeltakerException(opprettAvtale.getDeltakerFnr(), Feilkode.IKKE_TILGANG_TIL_DELTAKER_ARBEIDSGIVER);
        }

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
