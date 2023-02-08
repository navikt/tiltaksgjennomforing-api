package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetDatoErTilbakeITidException;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


public class Arbeidsgiver extends Avtalepart<Fnr> {
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final VeilarbArenaClient veilarbArenaClient;
    static String tekstAvtaleVenterPaaDinGodkjenning = "Deltakeren trenger ikke å godkjenne avtalen før du gjør det. ";
    static String ekstraTekstAvtaleVenterPaaDinGodkjenning = "Veilederen skal godkjenne til slutt.";
    static String tekstTiltaketErAvsluttet = "Hvis du har spørsmål må du kontakte NAV.";
    private final Map<BedriftNr, Collection<Tiltakstype>> tilganger;
    private final Set<AltinnReportee> altinnOrganisasjoner;

    public Arbeidsgiver(
            Fnr identifikator,
            Set<AltinnReportee> altinnOrganisasjoner,
            Map<BedriftNr, Collection<Tiltakstype>> tilganger,
            PersondataService persondataService,
            Norg2Client norg2Client,
            VeilarbArenaClient veilarbArenaClient
    ) {
        super(identifikator);
        this.altinnOrganisasjoner = altinnOrganisasjoner;
        this.tilganger = tilganger;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarbArenaClient = veilarbArenaClient;
    }

    private static boolean avbruttForMerEnn12UkerSiden(Avtale avtale) {
        return avtale.isAvbrutt() && avtale.getSistEndret().plus(84, ChronoUnit.DAYS).isBefore(Now.instant());
    }

    private static boolean sluttdatoPassertMedMerEnn12Uker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getGjeldendeInnhold().getSluttDato().plusWeeks(12).isBefore(Now.localDate());
    }

    private static Avtale fjernAvbruttGrunn(Avtale avtale) {
        avtale.setAvbruttGrunn(null);
        return avtale;
    }

    private static Avtale fjernAnnullertGrunn(Avtale avtale) {
        avtale.setAnnullertGrunn(null);
        return avtale;
    }

    private static Avtale fjernKvalifiseringsgruppe(Avtale avtale) {
        avtale.setKvalifiseringsgruppe(null);
        avtale.setFormidlingsgruppe(null);
        return avtale;
    }

    @Override
    protected void avvisDatoerTilbakeITid(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (!avtale.erUfordelt()) {
            return;
        }
        if (startDato != null && startDato.isBefore(Now.localDate())) {
            throw new VarighetDatoErTilbakeITidException();
        }
        if (sluttDato != null && sluttDato.isBefore(Now.localDate())) {
            throw new VarighetDatoErTilbakeITidException();
        }
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForArbeidsgiver(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return avtale.erGodkjentAvArbeidsgiver();
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return !avtale.erGodkjentAvVeileder();
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        avtale.opphevGodkjenningerSomArbeidsgiver();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.ARBEIDSGIVER;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetArbeidsgiver(getIdentifikator(), altinnOrganisasjoner, tilganger);
    }

    @Override
    public Collection<BedriftNr> identifikatorer() {
        return tilganger.keySet();
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        if (sluttdatoPassertMedMerEnn12Uker(avtale)) {
            return false;
        }
        if (avbruttForMerEnn12UkerSiden(avtale)) {
            return false;
        }
        return harTilgangPåTiltakIBedrift(avtale.getBedriftNr(), avtale.getTiltakstype());
    }

    private boolean harTilgangPåTiltakIBedrift(BedriftNr bedriftNr, Tiltakstype tiltakstype) {
        if (!tilganger.containsKey(bedriftNr)) {
            return false;
        }
        Collection<Tiltakstype> gyldigeTilgangerPåBedriftNr = tilganger.get(bedriftNr);
        return gyldigeTilgangerPåBedriftNr.contains(tiltakstype);
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (tilganger.isEmpty()) {
            return Collections.emptyList();
        }
        return avtaleRepository.findAllByBedriftNrIn(tilganger.keySet()).stream()
                .map(Arbeidsgiver::fjernAvbruttGrunn)
                .map(Arbeidsgiver::fjernAnnullertGrunn)
                .collect(Collectors.toList());
    }

    @Override
    public List<Avtale> hentAlleAvtalerMedLesetilgang(
            AvtaleRepository avtaleRepository,
            AvtalePredicate queryParametre,
            String sorteringskolonne,
            int skip,
            int limit
    ) {
        return super.hentAlleAvtalerMedLesetilgang(avtaleRepository, queryParametre, sorteringskolonne, skip, limit)
                .stream().map(Arbeidsgiver::fjernAvbruttGrunn).collect(Collectors.toList());
    }

    public List<Avtale> hentAvtalerForMinsideArbeidsgiver(AvtaleRepository avtaleRepository, BedriftNr bedriftNr) {
        return avtaleRepository.findAllByBedriftNr(bedriftNr).stream()
                .filter(this::harTilgang)
                .map(Arbeidsgiver::fjernAvbruttGrunn)
                .map(Arbeidsgiver::fjernAnnullertGrunn)
                .collect(Collectors.toList());
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        if (!harTilgangPåTiltakIBedrift(opprettAvtale.getBedriftNr(), opprettAvtale.getTiltakstype())) {
            throw new TilgangskontrollException("Har ikke tilgang på tiltak i valgt bedrift");
        }
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(opprettAvtale);
        leggTilGeografiskOgOppfølgingsenhet(
                avtale,
                persondataService.hentPersondata(avtale.getDeltakerFnr()),
                norg2Client,
                veilarbArenaClient
        );
        return avtale;
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        Avtale avtale = super.hentAvtale(avtaleRepository, avtaleId);
        fjernAvbruttGrunn(avtale);
        fjernAnnullertGrunn(avtale);
        fjernKvalifiseringsgruppe(avtale);
        return avtale;
    }

    public Avtale opprettMentorAvtale(OpprettMentorAvtale opprettMentorAvtale) {
        if (!harTilgangPåTiltakIBedrift(opprettMentorAvtale.getBedriftNr(), opprettMentorAvtale.getTiltakstype())) {
            throw new TilgangskontrollException("Har ikke tilgang på tiltak i valgt bedrift");
        };

        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(opprettMentorAvtale);
        leggTilGeografiskOgOppfølgingsenhet(
                avtale,
                persondataService.hentPersondata(avtale.getDeltakerFnr()),
                norg2Client,
                veilarbArenaClient
        );
        return avtale;
    }
}
