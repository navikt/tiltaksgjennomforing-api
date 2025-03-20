package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return avtale.getDeltakerFnr().equals(getIdentifikator());
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return this::harTilgangTilAvtale;
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
        throw new TilgangskontrollException("Deltaker kan ikke oppheve godkjenninger");
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
