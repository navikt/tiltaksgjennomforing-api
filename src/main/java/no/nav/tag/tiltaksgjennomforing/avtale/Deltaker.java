package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.exceptions.RolleHarIkkeTilgangException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.data.domain.Page;
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
        if (avtale.getOpphav() == Avtaleopphav.ARENA && !avtale.erAvtaleInngått()) {
            return false;
        }
        return super.avtalenEksisterer(avtale);
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(
        AvtaleRepository avtaleRepository,
        AvtaleQueryParameter queryParametre,
        Pageable pageable
    ) {
        return avtaleRepository.findAllByDeltakerFnrAndFeilregistrertIsFalse(getIdentifikator(), pageable);
    }

    @Override
    public AvtaleDTO maskerFelterForAvtalepart(AvtaleDTO avtale) {
        if (avtale.tiltakstype() == Tiltakstype.MENTOR) {
            return avtale.toBuilder()
                .mentorFnr(null)
                .gjeldendeInnhold(avtale.gjeldendeInnhold().toBuilder()
                    .arbeidsgiveravgift(null)
                    .arbeidsgiveravgiftBelop(null)
                    .feriepengesats(null)
                    .feriepengerBelop(null)
                    .lonnstilskuddProsent(null)
                    .manedslonn(null)
                    .mentorTimelonn(null)
                    .otpSats(null)
                    .otpBelop(null)
                    .build())
                .build();
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
