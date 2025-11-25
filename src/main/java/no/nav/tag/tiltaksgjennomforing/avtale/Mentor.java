package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RolleHarIkkeTilgangException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode.IKKE_TILGANG_TIL_DELTAKER;

public class Mentor extends Avtalepart<Fnr> {

    public Mentor(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        if (avtale.harSluttdatoPassertMedMerEnn12Uker()) {
            return new Tilgang.Avvis(
                Avslagskode.SLUTTDATO_PASSERT,
                "Sluttdato har passert med mer enn 12 uker"
            );
        }
        if (avtale.erAnnullertForMerEnn12UkerSiden()) {
            return new Tilgang.Avvis(
                Avslagskode.UTGATT,
                "Annullert for mer enn 12 uker siden"
            );
        }
        if (Utils.equalsMenIkkeNull(avtale.getMentorFnr(), getIdentifikator())) {
            return new Tilgang.Tillat();
        }
        return new Tilgang.Avvis(IKKE_TILGANG_TIL_DELTAKER, "Mentor fnr stemmer ikke med innlogget bruker");
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return avtale -> harTilgangTilAvtale(avtale).erTillat();
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository, avtaleId);
        if (!avtale.erGodkjentTaushetserklæringAvMentor())
            throw new FeilkodeException(Feilkode.IKKE_TILGANG_TIL_AVTALE);
        skjulSensitivData(avtale);
        skjulMentorFelterForMentor(avtale);
        return avtale;
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        Page<Avtale> avtaler = avtaleRepository.findAllByMentorFnr(
            getIdentifikator(),
            pageable
        );
        return avtaler;
    }

    private void skjulMentorFelterForMentor(Avtale avtale){
            avtale.getGjeldendeInnhold().setArbeidsgiveravgift(null);
            avtale.getGjeldendeInnhold().setArbeidsgiveravgiftBelop(null);
            avtale.getGjeldendeInnhold().setFeriepengesats(null);
            avtale.getGjeldendeInnhold().setFeriepengerBelop(null);
            avtale.getGjeldendeInnhold().setLonnstilskuddProsent(null);
            avtale.getGjeldendeInnhold().setManedslonn(null);
            avtale.getGjeldendeInnhold().setOtpSats(null);
            avtale.getGjeldendeInnhold().setOtpBelop(null);
            avtale.getGjeldendeInnhold().setMentorValgtLonnstype(null);
            avtale.getGjeldendeInnhold().setMentorValgtLonnstypeBelop(null);
            avtale.getTilskuddPeriode().forEach(tilskuddPeriode ->
                tilskuddPeriode.setBeløp(null));
    }

    @Override
    public void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForMentor(getIdentifikator());
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
        return Avtalerolle.MENTOR;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetMentor(getIdentifikator());
    }

    private void skjulSensitivData(Avtale avtale) {
        avtale.setAnnullertGrunn(null);
        avtale.setDeltakerFnr(null);
    }
}
