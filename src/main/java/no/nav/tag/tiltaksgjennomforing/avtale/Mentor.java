package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class Mentor extends Avtalepart<Fnr> {

    public Mentor(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return avtale.getMentorFnr().equals(getIdentifikator());
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        return this::harTilgangTilAvtale;
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
        Page<Avtale> avtaler = avtaleRepository.findAllByMentorFnrAndFeilregistrertIsFalse(getIdentifikator(), pageable);
        return avtaler;
    }

    @Override
    AvtaleMinimalListevisning skjulData(AvtaleMinimalListevisning avtaleMinimalListevisning) {
        return avtaleMinimalListevisning;
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
