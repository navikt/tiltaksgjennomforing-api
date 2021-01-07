package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.List;
import java.util.stream.Collectors;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
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
            return getAvtalesMedGodkjentTilskuddPerioder(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNotNull());
        }
        return getAvtalesMedGodkjentTilskuddPerioder(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNull());
    }

    @NotNull
    private List<Avtale> getAvtalesMedGodkjentTilskuddPerioder(List<TilskuddPeriode> allByGodkjentTidspunktIsNull) {
        return allByGodkjentTidspunktIsNull
            .stream().map(tilskudd -> tilskudd.getAvtaleInnhold().getAvtale())
            .collect(Collectors.toList());
    }

    //TODO: Fiks tomme overrides

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
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

    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.BESLUTTER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {

    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
