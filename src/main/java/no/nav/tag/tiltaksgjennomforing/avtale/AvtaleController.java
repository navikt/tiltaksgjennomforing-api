package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.dokgen.DokgenService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
@Timed
@Slf4j
@RequiredArgsConstructor
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final EregService eregService;
    private final VeilarbArenaClient veilarbArenaClient;
    private final Norg2Client norg2Client;
    private final KontoregisterService kontoregisterService;
    private final DokgenService dokgenService;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;

    @GetMapping("/{avtaleId}")
    public Avtale hent(@PathVariable("avtaleId") UUID id, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        return avtalepart.hentAvtale(avtaleRepository, id);
    }

    @GetMapping
    public List<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil(AvtalePredicate queryParametre,
                                                                    @RequestParam(value = "sorteringskolonne", required = false, defaultValue = Avtale.Fields.sistEndret) String sorteringskolonne,
                                                                    @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        List<Avtale> avtaler = avtalepart.hentAlleAvtalerMedLesetilgang(avtaleRepository, queryParametre);
        return AvtaleSorterer.sorterAvtaler(sorteringskolonne, avtaler);
    }

    @GetMapping("/{avtaleId}/status-detaljer")
    public AvtaleStatusDetaljer hentAvtaleStatusDetaljer(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        return avtalepart.statusDetaljerForAvtale(avtale);
    }

    @GetMapping("/{avtaleId}/pdf")
    public HttpEntity<?> hentAvtalePdf(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtalepart.hentAvtale(avtaleRepository, avtaleId);

        if (!avtale.erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LASTE_NED_PDF);
        }

        byte[] avtalePdf = dokgenService.avtalePdf(avtale);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_PDF);
        header.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=Avtale om " + avtale.getTiltakstype().getBeskrivelse() + ".pdf");
        header.setContentLength(avtalePdf.length);

        return new HttpEntity<>(avtalePdf, header);
    }

    @PutMapping("/{avtaleId}")
    @Transactional
    public ResponseEntity<?> endreAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody EndreAvtale endreAvtale,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart)
    {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale, tilskuddsperiodeConfig.getTiltakstyper());
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @PutMapping("/{avtaleId}/dry-run")
    public Avtale endreAvtaleDryRun(@PathVariable("avtaleId") UUID avtaleId,
                                    @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                    @RequestBody EndreAvtale endreAvtale, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale, tilskuddsperiodeConfig.getTiltakstyper());
        return avtale;
    }

    @PostMapping("/{avtaleId}/opphev-godkjenninger")
    @Transactional
    public void opphevGodkjenninger(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn")
    @Transactional
    public void godkjenn(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.godkjennAvtale(sistEndret, avtale);
        avtaleRepository.save(avtale);
    }


    // Arbeidsgiver-operasjoner

    @GetMapping("/min-side-arbeidsgiver")
    public List<Avtale> hentAlleAvtalerForMinSideArbeidsgiver(@RequestParam("bedriftNr") BedriftNr bedriftNr) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        return arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, bedriftNr);
    }

    @GetMapping(path = "/{avtaleId}/kontonummer-arbeidsgiver")
    public String hentBedriftKontonummer(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtalepart.hentAvtale(avtaleRepository, avtaleId);
        return kontoregisterService.hentKontonummer(avtale.getBedriftNr().asString());
    }

    @PostMapping("/opprett-som-arbeidsgiver")
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomArbeidsgiver(@RequestBody OpprettAvtale opprettAvtale) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        avtale.setEnhetOppfolging(veilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString()));
        arbeidsgiver.leggTilOppfølingEnhetsnavn(avtale, norg2Client);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }


    // Veileder-operasjoner

    @PostMapping
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomVeileder(@RequestBody OpprettAvtale opprettAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Oppfølgingsstatus oppfølgingsstatus = veilarbArenaClient.sjekkOgHentOppfølgingStatus(opprettAvtale);
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        veileder.leggTilOppfølingEnhetsnavn(avtale, norg2Client);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{avtaleId}/forkort")
    @Transactional
    public void forkortAvtale(@PathVariable("avtaleId") UUID avtaleId,
                              @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                              @RequestBody ForkortAvtale forkortAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), forkortAvtale.getGrunn(), forkortAvtale.getAnnetGrunn());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/forkort-dry-run")
    public Avtale forkortAvtaleDryRun(@PathVariable("avtaleId") UUID avtaleId,
                                      @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                      @RequestBody ForkortAvtale forkortAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        // Er ikke nødvending med en reell grunn siden det ikke påvirker tilskuddsperioder
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), "dry run", "");
        return avtale;
    }

    @PostMapping("/{avtaleId}/forleng")
    @Transactional
    public void forlengAvtale(@PathVariable("avtaleId") UUID avtaleId,
                              @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                              @RequestBody ForlengAvtale forlengAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(forlengAvtale.getSluttDato(), avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/forleng-dry-run")
    public Avtale forlengAvtaleDryRun(@PathVariable("avtaleId") UUID avtaleId,
                                      @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                      @RequestBody ForlengAvtale forlengAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(forlengAvtale.getSluttDato(), avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/endre-maal")
    @Transactional
    public void endreMål(@PathVariable("avtaleId") UUID avtaleId,
                         @RequestBody EndreMål endreMål) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreMål(endreMål, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-stillingbeskrivelse")
    @Transactional
    public void endreStillingbeskrivelse(@PathVariable("avtaleId") UUID avtaleId,
                                         @RequestBody EndreStillingsbeskrivelse endreStillingsbeskrivelse) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreStillingbeskrivelse(endreStillingsbeskrivelse, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-oppfolging-og-tilrettelegging")
    @Transactional
    public void endreOppfølgingOgTilrettelegging(@PathVariable("avtaleId") UUID avtaleId,
                                                 @RequestBody EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-kontaktinfo")
    @Transactional
    public void endreKontaktinfo(@PathVariable("avtaleId") UUID avtaleId,
                                 @RequestBody EndreKontaktInformasjon endreKontaktInformasjon) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreKontaktinfo(endreKontaktInformasjon, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning")
    @Transactional
    public void endreTilskuddsberegning(@PathVariable("avtaleId") UUID avtaleId,
                                        @RequestBody EndreTilskuddsberegning endreTilskuddsberegning) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning-dry-run")
    public Avtale endreTilskuddsberegningDryRun(@PathVariable("avtaleId") UUID avtaleId,
                                                @RequestBody EndreTilskuddsberegning endreTilskuddsberegning) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/send-tilbake-til-beslutter")
    @Transactional
    public void sendTilbakeTilBeslutter(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.sendTilbakeTilBeslutter(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping({"/{avtaleId}/godkjenn-paa-vegne-av", "/{avtaleId}/godkjenn-paa-vegne-av-deltaker"})
    @Transactional
    public void godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId,
                                  @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltaker(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvArbeidsgiver(@PathVariable("avtaleId") UUID avtaleId,
                                              @RequestBody GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvGrunn) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-deltaker-og-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvDeltakerOgArbeidsgiver(@PathVariable("avtaleId") UUID avtaleId,
                                                        @RequestBody GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvGrunn) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltakerOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/gjenopprett")
    @Transactional
    public void gjenopprettAvtale(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.gjenopprettAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/avbryt")
    @Transactional
    public void avbryt(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @RequestBody AvbruttInfo avbruttInfo) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.avbrytAvtale(sistEndret, avbruttInfo, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/annuller")
    @Transactional
    public void annuller(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @RequestBody AnnullertInfo annullertInfo) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.annullerAvtale(sistEndret, annullertInfo.getAnnullertGrunn(), avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/slettemerk")
    @Transactional
    public void slettemerk(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.slettemerk(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/laas-opp")
    @Transactional
    public void laasOpp(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.låsOppAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/del-med-avtalepart")
    @Transactional
    public void delAvtaleMedAvtalepart(@PathVariable("avtaleId") UUID avtaleId, @RequestBody Avtalerolle avtalerolle) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.delAvtaleMedAvtalepart(avtalerolle, avtale);
        avtaleRepository.save(avtale);
    }

    @PutMapping("/{avtaleId}/overta")
    @Transactional
    public void settNyVeilederPåAvtale(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.overtaAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-tilskuddsperiode")
    @Transactional
    public void godkjennTilskuddsperiode(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjennTilskuddsperiodeRequest godkjennTilskuddsperiodeRequest) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.godkjennTilskuddsperiode(avtale, godkjennTilskuddsperiodeRequest.getEnhet());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-kostnadssted")
    @Transactional
    public Avtale endreKostnadssted(@PathVariable("avtaleId") UUID avtaleId, @RequestBody EndreKostnadsstedRequest endreKostnadsstedRequest) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.oppdatereKostnadssted(avtale, norg2Client, endreKostnadsstedRequest.getEnhet());
        avtaleRepository.save(avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/avslag-tilskuddsperiode")
    @Transactional
    public void avslåTilskuddsperiode(@PathVariable("avtaleId") UUID avtaleId, @RequestBody AvslagRequest avslagRequest) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.avslåTilskuddsperiode(avtale, avslagRequest.getAvslagsårsaker(), avslagRequest.getAvslagsforklaring());
        avtaleRepository.save(avtale);
    }
}
