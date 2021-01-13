package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.apache.commons.lang3.NotImplementedException;

public class Beslutter extends Avtalepart<NavIdent> {

    private TilgangskontrollService tilgangskontrollService;
    private TilskuddPeriodeRepository tilskuddPeriodeRepository;

    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, TilskuddPeriodeRepository tilskuddPeriodeRepository) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.tilskuddPeriodeRepository = tilskuddPeriodeRepository;
    }

    public void godkjennTilskuddsperiode(Avtale avtale, UUID tilskuddPeriodeId) {
        sjekkTilgang(avtale);
        TilskuddPeriode tilskuddPeriode = avtale.getTilskuddPeriode().stream().filter(it -> it.getId().equals(tilskuddPeriodeId)).findFirst().orElseThrow();
        tilskuddPeriode.godkjenn(getIdentifikator());
    }

    public void avslåTilskuddsperiode(Avtale avtale, UUID tilskuddPeriodeId, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkTilgang(avtale);
        TilskuddPeriode tilskuddPeriode = avtale.getTilskuddPeriode().stream().filter(it -> it.getId().equals(tilskuddPeriodeId)).findFirst().orElseThrow();
        tilskuddPeriode.avslå(getIdentifikator(), avslagsårsaker, avslagsforklaring);
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        TilskuddPeriodeStatus tilskuddPeriodeStatus =
            queryParametre.getTilskuddPeriodeStatus() == null ? TilskuddPeriodeStatus.UBEHANDLET : queryParametre.getTilskuddPeriodeStatus();
        return tilskuddPeriodeRepository.findAllByStatus(tilskuddPeriodeStatus)
            .stream().map(tilskudd -> tilskudd.getAvtaleInnhold().getAvtale())
            .filter(avtale -> erTiltakstype(queryParametre, avtale))
            .peek(avtale -> avtale.setTilskuddPeriodeStatus(tilskuddPeriodeStatus.value()))
            .distinct()
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
        throw new TilgangskontrollException("Beslutter kan ikke godkjenne avtaler");
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        throw new NotImplementedException();
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
        throw new TilgangskontrollException("Beslutter kan ikke oppheve godkjenninger av avtaler");
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.BESLUTTER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {
        throw new TilgangskontrollException("Beslutter kan ikke låse opp avtaler");
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
