package no.nav.tag.tiltaksgjennomforing.controller;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.ereg.EregService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac.TilgangskontrollService;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.domene.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
@Transactional
@Timed
@RequiredArgsConstructor
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final EregService eregService;
    private final TilgangUnderPilotering tilgangUnderPilotering;
    private final TilgangskontrollService tilgangskontrollService; 

    @GetMapping("/{avtaleId}")
    public ResponseEntity<Avtale> hent(@PathVariable("avtaleId") UUID id) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        innloggetBruker.sjekkLeseTilgang(avtale);
        return ResponseEntity.ok(avtale);
    }

    @GetMapping
    public Iterable<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        List<Avtale> avtaler = new ArrayList<>();
        for (Avtale avtale : avtaleRepository.findAll()) {
            if (bruker.harLeseTilgang(avtale)) {
                avtaler.add(avtale);
            }
        }
        return avtaler;
    }

    @PostMapping
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        tilgangUnderPilotering.sjekkTilgang(innloggetNavAnsatt.getIdentifikator());
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(innloggetNavAnsatt, opprettAvtale.getDeltakerFnr());

        Avtale avtale = innloggetNavAnsatt.opprettAvtale(opprettAvtale);
        avtale.setBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{avtaleId}")
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                      @RequestHeader("If-Match") Integer versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.endreAvtale(versjon, endreAvtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon().toString()).build();
    }

    @GetMapping(value = "/{avtaleId}/rolle")
    public ResponseEntity<Avtalerolle> hentRolle(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkLeseTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        return ResponseEntity.ok(avtalepart.rolle());
    }

    @PostMapping(value = "/{avtaleId}/opphev-godkjenninger")
    public ResponseEntity opphevGodkjenninger(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.opphevGodkjenninger();
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/godkjenn")
    public ResponseEntity godkjenn(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader("If-Match") Integer versjon) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale); 
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennAvtale(versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/godkjenn-paa-vegne-av")
    public ResponseEntity godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn, @RequestHeader("If-Match") Integer versjon) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennPaVegneAvDeltaker(paVegneAvGrunn, versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }
}
