package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.AdGruppeTilganger;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Beslutter extends Avtalepart<NavIdent> implements InternBruker {
    private final Norg2Client norg2Client;
    private final TilgangskontrollService tilgangskontrollService;
    private final UUID azureOid;
    private final Set<NavEnhet> navEnheter;
    private final PersondataService persondataService;
    private final AdGruppeTilganger adGruppeTilganger;

    public Beslutter(
        NavIdent identifikator,
        UUID azureOid,
        Set<NavEnhet> navEnheter,
        TilgangskontrollService tilgangskontrollService,
        Norg2Client norg2Client,
        PersondataService persondataService,
        AdGruppeTilganger adGruppeTilganger
    ) {
        super(identifikator);
        this.azureOid = azureOid;
        this.navEnheter = navEnheter;
        this.tilgangskontrollService = tilgangskontrollService;
        this.norg2Client = norg2Client;
        this.persondataService = persondataService;
        this.adGruppeTilganger = adGruppeTilganger;
    }

    public void godkjennTilskuddsperiode(Avtale avtale, String enhet) {
        sjekkTilgang(avtale);
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhet(enhet);

        if (response == null) {
            throw new FeilkodeException(Feilkode.ENHET_FINNES_IKKE);
        }
        avtale.godkjennTilskuddsperiode(getIdentifikator(), enhet);
    }

    public void avslåTilskuddsperiode(Avtale avtale, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkTilgang(avtale);
        avtale.avslåTilskuddsperiode(getIdentifikator(), avslagsårsaker, avslagsforklaring);
    }

    public void setOmAvtalenKanEtterregistreres(Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.togglegodkjennEtterregistrering(getIdentifikator());
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        return tilgangskontrollService.hentSkrivetilgang(this, avtale.getDeltakerFnr());
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        Map<Fnr, Boolean> map = tilgangskontrollService.harSkrivetilgangTilAvtaler(this, avtaler);
        return avtale -> {
            boolean resultat = map.getOrDefault(avtale.getDeltakerFnr(), false);
            if (!resultat) {
                log.info("Har ikke tilgang til avtalenr {}, id: {}", avtale.getAvtaleNr(), avtale.getId());
            }
            return resultat;
        };
    }

    public boolean harTilgangTilFnr(Fnr fnr) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(this, fnr);
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
        return avtaleRepository.findAllByAvtaleNrAndFeilregistrertIsFalse(queryParametre.getAvtaleNr(), pageable);
    }

    private Integer getPlussdato() {
        return ((int) ChronoUnit.DAYS.between(Now.localDate(), Now.localDate().plusMonths(3)));
    }

    Page<BegrensetBeslutterAvtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
            AvtaleRepository avtaleRepository,
            AvtaleQueryParameter queryParametre,
            String sorteringskolonne,
            Integer page,
            Integer size,
            String sorteringOrder
    ) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            AvtaleSorterer.getSortingOrder(Avtalerolle.BESLUTTER, sorteringskolonne, sorteringOrder)
        );
        Set<String> navEnheter = hentNavEnheter();
        if (navEnheter.isEmpty()) {
            throw new NavEnhetIkkeFunnetException();
        }

        Set<Tiltakstype> tiltakstyper = Optional.ofNullable(queryParametre.getTiltakstype())
            .map(Set::of)
            .orElse(Set.of(
                Tiltakstype.SOMMERJOBB,
                Tiltakstype.VARIG_LONNSTILSKUDD,
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
                Tiltakstype.VTAO
            ));

        Page<BeslutterOversiktEntity> avtaler = avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            queryParametre.getTilskuddPeriodeStatus(),
            Now.localDate().plusDays(getPlussdato()),
            tiltakstyper,
            Optional.ofNullable(queryParametre.getNavEnhet()).map(Set::of).orElse(navEnheter),
            queryParametre.getBedriftNr(),
            queryParametre.getAvtaleNr(),
            queryParametre.harReturnertSomKanBehandles(),
            pageable
        );

        boolean isSkalHenteDiskresjonskoder = adGruppeTilganger.fortroligAdresse() || adGruppeTilganger.strengtFortroligAdresse();
        Map<Fnr, Diskresjonskode> diskresjon = isSkalHenteDiskresjonskoder ? persondataService.hentDiskresjonskoder(
            avtaler.stream().map(BeslutterOversiktEntity::getDeltakerFnr).collect(Collectors.toSet())
        ) : Map.of();

        List<BegrensetBeslutterAvtale> begrensedeAvtaler = avtaler
            .stream()
            .filter(oversiktDTO -> harTilgangTilFnr(oversiktDTO.getDeltakerFnr()))
            .map(avtale -> BegrensetBeslutterAvtale.fraEntitetMedDiskresjonskode(
                avtale,
                diskresjon.get(avtale.getDeltakerFnr())
            ))
            .toList();

        return new PageImpl<>(begrensedeAvtaler, avtaler.getPageable(), avtaler.getTotalElements());
    }

    private Set<String> hentNavEnheter() {
        return this.navEnheter.stream().map(NavEnhet::getVerdi).collect(Collectors.toSet());
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
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator(), navEnheter);
    }

    @Override
    public UUID getAzureOid() {
        return azureOid;
    }

    @Override
    public NavIdent getNavIdent() {
        return getIdentifikator();
    }
}
