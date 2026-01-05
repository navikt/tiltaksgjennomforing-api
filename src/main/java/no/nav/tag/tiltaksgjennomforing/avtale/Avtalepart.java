package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilAvtaleException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@AllArgsConstructor
@Slf4j
@Data
public abstract class Avtalepart<T extends Identifikator> {

    private final T identifikator;

    public boolean avtalenEksisterer(Avtale avtale) {
        return !avtale.isFeilregistrert();
    }

    protected boolean mentorAvtaleErKlarForVisningForEksterne(Avtale avtale) {
        return avtale.felterSomIkkeErFyltUt().stream()
            .allMatch(x -> x.equals("harFamilietilknytning"));
    }

    abstract Tilgang harTilgangTilAvtale(Avtale avtale);

    abstract Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler);

    abstract Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable);

    public Page<BegrensetAvtale> hentBegrensedeAvtalerMedLesetilgang(
        AvtaleRepository avtaleRepository,
        AvtaleQueryParameter queryParametre,
        Pageable pageable
    ) {
        Page<Avtale> avtaler = hentAvtalerMedLesetilgang(avtaleRepository, queryParametre, pageable);

        List<BegrensetAvtale> begrensedeAvtaler = avtaler
            .getContent()
            .stream()
            .map(BegrensetAvtale::fraAvtale)
            .toList();

        return new PageImpl<>(begrensedeAvtaler, pageable, avtaler.getTotalElements());
    }

    protected final Page<Avtale> hentAvtalerMedLesetilgang(
        AvtaleRepository avtaleRepository,
        AvtaleQueryParameter queryParametre,
        Pageable pageable
    ) {
        Page<Avtale> avtaler = hentAlleAvtalerMedMuligTilgang(avtaleRepository, queryParametre, pageable);

        List<Avtale> avtalerMedTilgang = avtaler.getContent()
            .stream()
            .filter(this::avtalenEksisterer)
            .filter(this.harTilgangTilAvtale(avtaler.getContent()))
            .toList();

        if (queryParametre.erSokPaEnkeltperson() && avtalerMedTilgang.isEmpty()) {
            avtaler.getContent().forEach(this::sjekkTilgang);
        }

        return new PageImpl<>(avtalerMedTilgang, avtaler.getPageable(), avtaler.getTotalElements());
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

    public void godkjennAvtale(Avtale avtale) {
        sjekkTilgang(avtale);
        godkjennForAvtalepart(avtale);
    }

    public void sjekkTilgang(Avtale avtale) {
        if (!avtalenEksisterer(avtale)) {
            throw new RessursFinnesIkkeException();
        }
        if (harTilgangTilAvtale(avtale) instanceof Tilgang.Avvis avvis) {
            switch (avvis.tilgangskode()) {
                case STRENGT_FORTROLIG_ADRESSE -> throw new IkkeTilgangTilDeltakerException(
                    avtale.getDeltakerFnr(),
                    Feilkode.IKKE_TILGANG_TIL_DELTAKER_STRENGT_FORTROLIG
                );
                case FORTROLIG_ADRESSE -> throw new IkkeTilgangTilDeltakerException(
                    avtale.getDeltakerFnr(),
                    Feilkode.IKKE_TILGANG_TIL_DELTAKER_FORTROLIG
                );
                case EGNE_ANSATTE -> throw new IkkeTilgangTilDeltakerException(
                    avtale.getDeltakerFnr(),
                    Feilkode.IKKE_TILGANG_TIL_DELTAKER_SKJERMET
                );
                default -> throw new IkkeTilgangTilAvtaleException(avtale);
            }
        }
    }

    public void endreAvtale(
        EndreAvtale endreAvtale,
        Avtale avtale
    ) {
        endreAvtale(endreAvtale, avtale, () -> {});
    }

    public void endreAvtale(
            EndreAvtale endreAvtale,
            Avtale avtale,
            Runnable kjorForEndring
    ) {
        sjekkTilgang(avtale);
        if (!kanEndreAvtale()) {
            throw new KanIkkeEndreException();
        }
        avvisDatoerTilbakeITid(avtale, endreAvtale.getStartDato(), endreAvtale.getSluttDato());
        kjorForEndring.run();
        avtale.endreAvtale(endreAvtale, rolle(), identifikator);
    }

    protected void avvisDatoerTilbakeITid(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
    }

    protected abstract Avtalerolle rolle();

    public void opphevGodkjenninger(Avtale avtale) {
        sjekkTilgang(avtale);
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

    protected void hentGeoEnhetFraNorg2(Avtale avtale, Norg2Client norg2Client, PersondataService persondataService) {
        persondataService
            .hentGeografiskTilknytning(avtale.getDeltakerFnr())
            .map(norg2Client::hentGeografiskEnhet)
            .ifPresent(enhet -> {
                avtale.setEnhetGeografisk(enhet.getEnhetNr());
                avtale.setEnhetsnavnGeografisk(enhet.getNavn());
            });
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

    protected boolean erMigrertMentorAvtale(Avtale avtale) {
        return avtale.getTiltakstype() == Tiltakstype.MENTOR
            && avtale.getOpphav() == Avtaleopphav.ARENA;
    }
}
