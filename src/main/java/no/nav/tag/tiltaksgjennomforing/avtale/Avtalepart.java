package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;

@AllArgsConstructor
@Slf4j
@Data
public abstract class Avtalepart<T extends Identifikator> {
    private final T identifikator;

    public boolean avtalenEksisterer(Avtale avtale) {
        return !avtale.isFeilregistrert() && !avtale.isSlettemerket();
    }

    abstract boolean harTilgangTilAvtale(Avtale avtale);

    abstract Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre, Pageable pageable);

    abstract AvtaleMinimalListevisning skjulData(AvtaleMinimalListevisning avtaleMinimalListevisning);

    public Map<String, Object> hentAlleAvtalerMedLesetilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre, Pageable pageable) {
        Page<Avtale> avtaler = hentAlleAvtalerMedMuligTilgang(avtaleRepository, queryParametre, pageable);

        List<Avtale> avtalerMedTilgang = avtaler.getContent().stream()
                .filter(this::avtalenEksisterer)
                .filter(this::harTilgangTilAvtale)
                .toList();

        List<AvtaleMinimalListevisning> listMinimal = avtalerMedTilgang.stream().map(AvtaleMinimalListevisning::fromAvtale).toList();

        // Fjern data her og ikke i entity
        listMinimal = listMinimal.stream().map(avtaleMinimal -> skjulData(avtaleMinimal)).toList();

        //TODO: ENDRE TOTAL ITEMS OG TOTAL PAGES
        return Map.ofEntries(
                entry("avtaler", listMinimal),
                entry("size", avtaler.getSize()),
                entry("currentPage", avtaler.getNumber()),
                entry("totalItems", avtaler.getTotalElements()),
                entry("totalPages", avtaler.getTotalPages())
        );
    }

    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        sjekkTilgang(avtale);
        return avtale;
    }

    public Avtale hentAvtaleFraAvtaleNr(AvtaleRepository avtaleRepository, int avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow(RessursFinnesIkkeException::new);
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

    abstract boolean kanOppheveGodkjenninger(Avtale avtale);

    abstract void opphevGodkjenningerSomAvtalepart(Avtale avtale);

    public void godkjennAvtale(Instant sistEndret, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.sjekkSistEndret(sistEndret);
        godkjennForAvtalepart(avtale);
    }

    public void sjekkTilgang(Avtale avtale) {
        if (!avtalenEksisterer(avtale)) {
            throw new RessursFinnesIkkeException();
        }
        if (!harTilgangTilAvtale(avtale)) {
            throw new TilgangskontrollException("Ikke tilgang til avtale");
        }
    }

    protected void avtalePartKanEndreAvtale() {
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
    }

    public void endreAvtale(
            Instant sistEndret,
            EndreAvtale endreAvtale,
            Avtale avtale,
            EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder
    ) {
        sjekkTilgang(avtale);
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
        avvisDatoerTilbakeITid(avtale, endreAvtale.getStartDato(), endreAvtale.getSluttDato());
        avtale.endreAvtale(sistEndret, endreAvtale, rolle(), tiltakstyperMedTilskuddsperioder, identifikator);
    }

    protected void sjekkTilgangOgEndreAvtale(
            Instant sistEndret,
            EndreAvtale endreAvtale,
            Avtale avtale,
            EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder
    ) {
        sjekkTilgang(avtale);
        avtalePartKanEndreAvtale();
        avvisDatoerTilbakeITid(avtale, endreAvtale.getStartDato(), endreAvtale.getSluttDato());
        avtale.endreAvtale(
                sistEndret,
                endreAvtale,
                rolle(),
                tiltakstyperMedTilskuddsperioder,
                identifikator
        );
    }

    protected void avvisDatoerTilbakeITid(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
    }

    protected abstract Avtalerolle rolle();

    public void opphevGodkjenninger(Avtale avtale) {
        if (!kanOppheveGodkjenninger(avtale)) {
            throw new KanIkkeOppheveException();
        }
        boolean AlleParterHarIkkeGodkjentAvtale = !avtale.erGodkjentAvVeileder() &&
                !avtale.erGodkjentAvArbeidsgiver() &&
                !avtale.erGodkjentAvDeltaker();

        if (AlleParterHarIkkeGodkjentAvtale) {
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

    protected void hentGeoEnhetFraNorg2(Avtale avtale, PdlRespons pdlRespons, Norg2Client norg2Client) {
        Norg2GeoResponse enhet = PersondataService.hentGeoLokasjonFraPdlRespons(pdlRespons)
                .map(norg2Client::hentGeografiskEnhet).orElse(null);
        if (enhet == null) return;
        avtale.setEnhetGeografisk(enhet.getEnhetNr());
        avtale.setEnhetsnavnGeografisk(enhet.getNavn());
    }

    protected void hentOppfølgingsenhetNavnFraNorg2(Avtale avtale, Norg2Client norg2Client) {
        if (avtale.getEnhetOppfolging() == null) return;
        if (avtale.getEnhetOppfolging().equals(avtale.getEnhetGeografisk())) {
            avtale.setEnhetsnavnOppfolging(avtale.getEnhetsnavnGeografisk());
        } else {
            final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhet(avtale.getEnhetOppfolging());
            if (response == null) return;
            avtale.setEnhetsnavnOppfolging(response.getNavn());
        }
    }

    public void settLonntilskuddProsentsats(Avtale avtale) {
        if (avtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD) {
            avtale.getGjeldendeInnhold().setLonnstilskuddProsent(
                    avtale.getKvalifiseringsgruppe().finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(
                            40,
                            60
                    )
            );
        }
    }
}
