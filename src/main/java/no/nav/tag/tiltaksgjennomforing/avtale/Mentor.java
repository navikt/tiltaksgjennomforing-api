package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Avslagskode;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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
        if (avtale.getMentorFnr().equals(getIdentifikator())) {
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
        return avtale;
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        final LocalDate dato12UkerTilbakeFraNå = Now.localDate().minusWeeks(12);
        Page<Avtale> avtaler = avtaleRepository.findAllByMentorFnr(
            getIdentifikator(),
            dato12UkerTilbakeFraNå,
            pageable
        );
        return avtaler;
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
        throw new TilgangskontrollException("Mentor kan ikke oppheve godkjenninger");
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
        avtale.setAvbruttGrunn(null);
        avtale.setDeltakerFnr(null);
    }
}
