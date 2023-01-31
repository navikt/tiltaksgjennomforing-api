package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.enhet.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.*;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentGeoLokasjonFraPdlRespons;

@AllArgsConstructor
@Slf4j
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy");

    public boolean harTilgang(Avtale avtale) {
        if (avtale.isSlettemerket()) {
            return false;
        }
        return harTilgangTilAvtale(avtale);
    }

    abstract boolean harTilgangTilAvtale(Avtale avtale);

    abstract List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre);

    public List<Avtale> hentAlleAvtalerMedLesetilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre, String sorteringskolonne, int skip, int limit) {
        return hentAlleAvtalerMedMuligTilgang(avtaleRepository, queryParametre).stream()
                .filter(queryParametre).filter(avtale -> !avtale.isFeilregistrert())
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne))
                .skip(skip)
                .limit(limit)
                .filter(this::harTilgang)
                .collect(Collectors.toList());
    }

    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        sjekkTilgang(avtale);
        return avtale;
    }

    public List<AvtaleInnhold> hentAvtaleVersjoner(AvtaleRepository avtaleRepository, AvtaleInnholdRepository avtaleInnholdRepository, UUID avtaleId) {
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        sjekkTilgang(avtale);
        return avtaleInnholdRepository.findAllByAvtale(avtale);
    }

    abstract void godkjennForAvtalepart(Avtale avtale);

    abstract boolean kanEndreAvtale();

    public abstract boolean erGodkjentAvInnloggetBruker(Avtale avtale);

    abstract boolean kanOppheveGodkjenninger(Avtale avtale);

    abstract void opphevGodkjenningerSomAvtalepart(Avtale avtale);

    public void godkjennAvtale(Instant sistEndret, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.sjekkSistEndret(sistEndret);
        godkjennForAvtalepart(avtale);
    }

    public void sjekkTilgang(Avtale avtale) {
        if (!harTilgang(avtale)) {
            throw new TilgangskontrollException("Ikke tilgang til avtale");
        }
    }

    public void endreAvtale(Instant sistEndret, EndreAvtale endreAvtale, Avtale avtale, EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder) {
        sjekkTilgang(avtale);
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
        avvisDatoerTilbakeITid(avtale, endreAvtale.getStartDato(), endreAvtale.getSluttDato());
        avtale.endreAvtale(sistEndret, endreAvtale, rolle(), tiltakstyperMedTilskuddsperioder, identifikator);
    }

    protected void avvisDatoerTilbakeITid(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
    }

    protected abstract Avtalerolle rolle();

    public void opphevGodkjenninger(Avtale avtale) {
        if (!kanOppheveGodkjenninger(avtale)) {
            throw new KanIkkeOppheveException();
        }
        boolean ingenParterHarGodkjent = !avtale.erGodkjentAvVeileder() && !avtale.erGodkjentAvArbeidsgiver() && !avtale.erGodkjentAvDeltaker();
        if (ingenParterHarGodkjent) {
            throw new KanIkkeOppheveException();
        }
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE);
        }
        opphevGodkjenningerSomAvtalepart(avtale);
    }

    public abstract InnloggetBruker innloggetBruker();

    public Collection<? extends Identifikator> identifikatorer() {
        return List.of(getIdentifikator());
    }

    protected void leggTilGeografiskEnhet(Avtale avtale, PdlRespons pdlRespons, Norg2Client norg2Client) {
        Norg2GeoResponse enhet = hentGeoLokasjonFraPdlRespons(pdlRespons)
                .map(norg2Client::hentGeografiskEnhet)
                .orElse(null);
        if (enhet != null) {
            avtale.setEnhetGeografisk(enhet.getEnhetNr());
            avtale.setEnhetsnavnGeografisk(enhet.getNavn());
        }
    }

    protected void leggTilOppfølingEnhetsnavn(Avtale avtale, Norg2Client norg2Client) {
        if (avtale.getEnhetOppfolging() != null) {
            if (avtale.getEnhetOppfolging().equals(avtale.getEnhetGeografisk())) {
                avtale.setEnhetsnavnOppfolging(avtale.getEnhetsnavnGeografisk());
            } else {
                final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetsnavn(avtale.getEnhetOppfolging());
                if (response != null && response.getNavn() != null) {
                    avtale.setEnhetsnavnOppfolging(response.getNavn());
                }
            }
        }
    }

    public void sjekkOppfølgingStatusOgSettLønnstilskuddsprosentsats(Avtale avtale, VeilarbArenaClient veilarbArenaClient) {
        Oppfølgingsstatus oppfølgingsstatus = veilarbArenaClient.sjekkOgHentOppfølgingStatus(avtale);
        if (oppfølgingsstatus != null) {
            this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
            this.settLonntilskuddProsentsats(avtale);
        }
    }

    public void sjekkOgHentOppfølgingStatus(Avtale avtale, VeilarbArenaClient veilarbArenaClient) {
        Oppfølgingsstatus oppfølgingsstatus = veilarbArenaClient.sjekkOgHentOppfølgingStatus(avtale);
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
    }

    public void hentOppfølgingStatus(Avtale avtale, VeilarbArenaClient veilarbArenaClient) {
        if(avtale.getVeilederNavIdent() != null) {
            Oppfølgingsstatus oppfølgingsstatus = veilarbArenaClient.hentOppfølgingStatus(avtale.getDeltakerFnr().asString());
            if (
                    oppfølgingsstatus != null &&
                            (oppfølgingsstatus.getKvalifiseringsgruppe() != avtale.getKvalifiseringsgruppe() ||
                                    oppfølgingsstatus.getFormidlingsgruppe() != avtale.getFormidlingsgruppe())
            ) {
                this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
            }
        }
    }

    private void settOppfølgingsStatus(Avtale avtale, Oppfølgingsstatus oppfølgingsstatus) {
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        avtale.setKvalifiseringsgruppe(oppfølgingsstatus.getKvalifiseringsgruppe());
        avtale.setFormidlingsgruppe(oppfølgingsstatus.getFormidlingsgruppe());
    }

    public void settLonntilskuddProsentsats(Avtale avtale) {
        if (avtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD) {
            avtale.getGjeldendeInnhold().setLonnstilskuddProsent(avtale.getKvalifiseringsgruppe()
                    .finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60));
        }
    }
}
