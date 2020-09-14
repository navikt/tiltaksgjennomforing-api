package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.http.HttpHeaders;
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
    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;

    @GetMapping("/{avtaleId}")
    public Avtale hent(@PathVariable("avtaleId") UUID id, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        innloggetBruker.sjekkLeseTilgang(avtale);
        return avtale;
    }

    @GetMapping
    public List<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil(AvtalePredicate queryParametre, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> bruker = innloggingService.hentInnloggetBruker(innloggetPart);
        return bruker.hentAlleAvtalerMedLesetilgang(avtaleRepository, queryParametre);
    }

    @GetMapping("/min-side-arbeidsgiver")
    public List<Avtale> hentAlleAvtalerForMinSideArbeidsgiver(@RequestParam("bedriftNr") BedriftNr bedriftNr) {
        InnloggetArbeidsgiver innloggetArbeidsgiver = innloggingService.hentInnloggetArbeidsgiver();
        return innloggetArbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, bedriftNr);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        InnloggetVeileder innloggetVeileder = innloggingService.hentInnloggetVeileder();
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(innloggetVeileder, opprettAvtale.getDeltakerFnr());
        persondataService.sjekkGradering(opprettAvtale.getDeltakerFnr());
        Avtale avtale = innloggetVeileder.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        avtale.leggTilDeltakerNavn(persondataService.hentNavn(avtale.getDeltakerFnr()));
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/opprett-som-arbeidsgiver")
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomArbeidsgiver(@RequestBody OpprettAvtale opprettAvtale) {
        InnloggetArbeidsgiver innloggetArbeidsgiver = innloggingService.hentInnloggetArbeidsgiver();
        Avtale avtale = innloggetArbeidsgiver.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{avtaleId}/status-detaljer")
    public AvtaleStatusDetaljer hentAvtaleStatusDetaljer(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        return avtalepart.statusDetaljerForAvtale();
    }

    @PutMapping("/{avtaleId}")
    @Transactional
    public ResponseEntity<?> endreAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                         @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
                                         @RequestBody EndreAvtale endreAvtale, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.endreAvtale(sistEndret, endreAvtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @GetMapping("/{avtaleId}/rolle")
    public Avtalerolle hentRolle(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkLeseTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        return avtalepart.rolle();
    }


    @PostMapping("/{avtaleId}/gjenopprett")
    @Transactional
    public void gjenopprettAvtale(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetVeileder innloggetVeileder = innloggingService.hentInnloggetVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetVeileder.sjekkSkriveTilgang(avtale);
        Veileder veileder = innloggetVeileder.avtalepart(avtale);
        veileder.gjenopprettAvtale();
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/opphev-godkjenninger")
    @Transactional
    public void opphevGodkjenninger(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.opphevGodkjenninger();
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn")
    @Transactional
    public void godkjenn(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennAvtale(sistEndret);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av")
    @Transactional
    public void godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennPaVegneAvDeltaker(paVegneAvGrunn);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/avbryt")
    public void avbryt(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret, @RequestBody AvbruttInfo avbruttInfo) {
        InnloggetVeileder innloggetVeileder = innloggingService.hentInnloggetVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetVeileder.sjekkSkriveTilgang(avtale);
        Veileder veileder = innloggetVeileder.avtalepart(avtale);
        veileder.avbrytAvtale(sistEndret, avbruttInfo);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/laas-opp")
    @Transactional
    public void laasOpp(@PathVariable("avtaleId") UUID avtaleId, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        InnloggetBruker<?> innloggetBruker = innloggingService.hentInnloggetBruker(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart<?> avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.låsOppAvtale();
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/del-med-avtalepart")
    @Transactional
    public void delAvtaleMedAvtalepart(@PathVariable("avtaleId") UUID avtaleId, @RequestBody Avtalerolle avtalerolle) {
        InnloggetVeileder innloggetVeileder = innloggingService.hentInnloggetVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetVeileder.sjekkLeseTilgang(avtale);
        Veileder veileder = innloggetVeileder.avtalepart(avtale);
        veileder.delAvtaleMedAvtalepart(avtalerolle);
        avtaleRepository.save(avtale);
    }
}
