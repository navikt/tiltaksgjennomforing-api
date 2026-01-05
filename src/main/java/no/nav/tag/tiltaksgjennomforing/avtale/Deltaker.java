package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.exceptions.RolleHarIkkeTilgangException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode.IKKE_TILGANG_TIL_DELTAKER;


public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository,avtaleId);
        return skjulMentorFelterForDeltaker(avtale);
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        return Utils.equalsMenIkkeNull(avtale.getDeltakerFnr(), getIdentifikator())
            ? new Tilgang.Tillat()
            : new Tilgang.Avvis(IKKE_TILGANG_TIL_DELTAKER, "Deltaker fnr stemmer ikke med innlogget bruker");
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return avtale -> harTilgangTilAvtale(avtale).erTillat();
    }

    @Override
    public boolean avtalenEksisterer(Avtale avtale) {
        if (erMigrertMentorAvtale(avtale) && !mentorAvtaleErKlarForVisningForEksterne(avtale)) {
            return false;
        }
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

    private Avtale skjulMentorFelterForDeltaker(Avtale avtale){
        if(avtale.getTiltakstype() == Tiltakstype.MENTOR) {
            avtale.setMentorFnr(null);
            avtale.getGjeldendeInnhold().setArbeidsgiveravgift(null);
            avtale.getGjeldendeInnhold().setArbeidsgiveravgiftBelop(null);
            avtale.getGjeldendeInnhold().setFeriepengesats(null);
            avtale.getGjeldendeInnhold().setFeriepengerBelop(null);
            avtale.getGjeldendeInnhold().setLonnstilskuddProsent(null);
            avtale.getGjeldendeInnhold().setManedslonn(null);
            avtale.getGjeldendeInnhold().setMentorTimelonn(null);
            avtale.getGjeldendeInnhold().setOtpSats(null);
            avtale.getGjeldendeInnhold().setOtpBelop(null);
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
