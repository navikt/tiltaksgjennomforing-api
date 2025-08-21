package no.nav.tag.tiltaksgjennomforing.avtale;


import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.exceptions.RolleHarIkkeTilgangException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode.IKKE_TILGANG_TIL_DELTAKER;


@Slf4j
public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository,avtaleId);
        return skjulMentorFødselsnummer(avtale);
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        log.info("Deltaker - harTilgangTilAvtale. Identifikator: {}, avtale {}, erLik: {}", getIdentifikator(), avtale.getDeltakerFnr(), avtale.getDeltakerFnr().equals(getIdentifikator()));
        return avtale.getDeltakerFnr().equals(getIdentifikator())
            ? new Tilgang.Tillat()
            : new Tilgang.Avvis(IKKE_TILGANG_TIL_DELTAKER, "Deltaker fnr stemmer ikke med innlogget bruker");
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return avtale -> harTilgangTilAvtale(avtale).erTillat();
    }

    @Override
    public boolean avtalenEksisterer(Avtale avtale) {
        return super.avtalenEksisterer(avtale);
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        Page<Avtale> avtalePage = avtaleRepository.findAllByDeltakerFnrAndFeilregistrertIsFalse(getIdentifikator(), pageable);

        List<Avtale> avtaleListe = avtalePage
            .stream()
            .filter(avtale -> avtale.getOpphav() != Avtaleopphav.ARENA || avtale.erAvtaleInngått())
            .toList();

        return new PageImpl<>(avtaleListe, avtalePage.getPageable(), avtaleListe.size());
    }

    private Avtale skjulMentorFødselsnummer(Avtale avtale){
        if(avtale.getTiltakstype() == Tiltakstype.MENTOR) {
            avtale.setMentorFnr(null);
            avtale.getGjeldendeInnhold().setMentorTimelonn(null);
        }
        return avtale;
    }

    @Override
    public void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForDeltaker(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return false;
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        throw new RolleHarIkkeTilgangException();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.DELTAKER;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetDeltaker(getIdentifikator());
    }
}
