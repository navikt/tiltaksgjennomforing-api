package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.List;
import java.util.stream.Collectors;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public class Beslutter extends Avtalepart<NavIdent> {

    private TilgangskontrollService tilgangskontrollService;
    private TilskuddPeriodeRepository tilskuddPeriodeRepository;

    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, TilskuddPeriodeRepository tilskuddPeriodeRepository) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.tilskuddPeriodeRepository = tilskuddPeriodeRepository;
    }

    public Beslutter(NavIdent identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (queryParametre.getErGodkjkentTilskuddPerioder() != null && queryParametre.getErGodkjkentTilskuddPerioder()) {
            return getAvtalesMedGodkjentTilskuddPerioder(queryParametre, tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNotNull());
        }
        return getAvtalesMedGodkjentTilskuddPerioder(queryParametre, tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNull());
    }

    @NotNull
    private List<Avtale> getAvtalesMedGodkjentTilskuddPerioder(AvtalePredicate queryParametre, List<TilskuddPeriode> allByGodkjentTidspunktIsNull) {
        return allByGodkjentTidspunktIsNull
            .stream().map(tilskudd -> tilskudd.getAvtaleInnhold().getAvtale())
            .filter(avtale -> erTiltakstype(queryParametre, avtale))
            .collect(Collectors.toList());

    }

    private boolean erTiltakstype(AvtalePredicate queryParametre, Avtale avtale) {
        if (queryParametre.getTiltakstype() == null) {
            return true;
        }
        return avtale.getTiltakstype().equals(queryParametre.getTiltakstype());
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        throw new NotImplementedException();
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        return null;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return false;
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return false;
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        throw new NotImplementedException();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.BESLUTTER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {
        throw new NotImplementedException();
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
