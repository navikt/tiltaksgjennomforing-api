package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.dokgen.DokgenService;
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
@RequiredArgsConstructor
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final EregService eregService;
    private final VeilarbArenaClient veilarbArenaClient;
    private final KontoregisterService kontoregisterService;
    private final DokgenService dokgenService;

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
                "inline; filename=Avtale om " + avtale.getTiltakstype().getNavn() + ".pdf");
        header.setContentLength(avtalePdf.length);

        return new HttpEntity<>(avtalePdf, header);
    }

    @PutMapping("/{avtaleId}")
    @Transactional
    public ResponseEntity<?> endreAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                         @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                         @RequestBody EndreAvtale endreAvtale, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale);
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
        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale);
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

    @PostMapping(path = "/{avtaleId}/set-kontonummer-for-arbeidsgiver-fra-kontoregister")
    public ResponseEntity<?> oppdatterBedriftKontonummer(@PathVariable("avtaleId") UUID avtaleId,@CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtalepart.hentAvtale(avtaleRepository,avtaleId);
        avtale.setArbeidsgiverKontonummer(kontoregisterService.hentKontonummer(avtale.getBedriftNr().asString()));
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @PostMapping("/opprett-som-arbeidsgiver")
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomArbeidsgiver(@RequestBody OpprettAvtale opprettAvtale) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        avtale.setEnhetOppfolging(veilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString()));
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }


    // Veileder-operasjoner

    @PostMapping
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomVeileder(@RequestBody OpprettAvtale opprettAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        avtale.setEnhetOppfolging(veilarbArenaClient.hentOppfølgingsEnhet(avtale.getDeltakerFnr().asString()));
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{avtaleId}/forleng")
    @Transactional
    public ResponseEntity<?> forlengAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                           @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                           @RequestBody ForlengAvtale forlengAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(sistEndret, forlengAvtale.getSluttDato(), avtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning")
    @Transactional
    public ResponseEntity<?> endreTilskuddsberegning(@PathVariable("avtaleId") UUID avtaleId,
                                           @RequestBody EndreTilskuddsberegning endreTilskuddsberegning) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(Instant.now(), endreTilskuddsberegning, avtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av")
    @Transactional
    public void godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltaker(paVegneAvGrunn, avtale);
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
    public void avbryt(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @RequestBody AvbruttInfo avbruttInfo) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.avbrytAvtale(sistEndret, avbruttInfo, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/slettemerk")
    public void slettemerk(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.slettemerk(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/laas-opp")
    @Transactional
    public void laasOpp(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
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
    public void godkjennTilskuddsperiode(@PathVariable("avtaleId") UUID avtaleId) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.godkjennTilskuddsperiode(avtale);
        avtaleRepository.save(avtale);
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
