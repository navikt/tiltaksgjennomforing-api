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
@Timed
@RequiredArgsConstructor
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final EregService eregService;
    private final TilgangUnderPilotering tilgangUnderPilotering;

    @GetMapping("/{avtaleId}")
    public ResponseEntity<Avtale> hent(@PathVariable("avtaleId") UUID id) {
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        innloggetBruker.sjekkTilgang(avtale);
        return ResponseEntity.ok(avtale);
    }

    @GetMapping
    public Iterable<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil() {
        InnloggetBruker bruker = innloggingService.hentInnloggetBruker();
        List<Avtale> avtaler = new ArrayList<>();
        for (Avtale avtale : avtaleRepository.findAll()) {
            if (bruker.harTilgang(avtale)) {
                avtaler.add(avtale);
            }
        }
        return avtaler;
    }

    @PostMapping
    @Transactional
    public ResponseEntity opprettAvtale(@RequestBody OpprettAvtale opprettAvtale) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        tilgangUnderPilotering.sjekkTilgang(innloggetNavAnsatt.getIdentifikator());
        Avtale avtale = innloggetNavAnsatt.opprettAvtale(opprettAvtale);
        avtale.setBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/opprettAvtaleRevisjon/{avtaleId}")
    @Transactional
    public ResponseEntity opprettAvtaleRevisjon(@RequestBody OpprettAvtale opprettAvtaleRevisjon,
                                                @PathVariable("avtaleId") UUID sisteVersjonAvtaleId
            /*, @RequestBody int versjon, @RequestBody UUID baseAvtaleId*/) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        tilgangUnderPilotering.sjekkTilgang(innloggetNavAnsatt.getIdentifikator());
        //Avtale sisteAvtaleVersjon = avtaleRepository.findById(sisteVersjonAvtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Avtale sisteAvtaleVersjon = new Avtale(opprettAvtaleRevisjon.getDeltakerFnr(), opprettAvtaleRevisjon.getBedriftNr(),
                innloggetNavAnsatt.getIdentifikator(), opprettAvtaleRevisjon.getBaseAvtaleId(), opprettAvtaleRevisjon.getRevisjon());//(RessursFinnesIkkeException::new);
        try {
            sisteAvtaleVersjon = avtaleRepository.findById(sisteVersjonAvtaleId).get();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("sisteVersjonAvtaleId " + sisteVersjonAvtaleId.toString());
            System.out.println("sisteAvtaleVersjon.getId()");
            System.out.println(sisteAvtaleVersjon.getId());
        }
       /* Avtale avtaleRevisjon = innloggetNavAnsatt.opprettAvtale(opprettAvtaleRevisjon, sisteVersjonAvtaleId,
                sisteAvtaleVersjon.getBaseAvtaleId(), sisteAvtaleVersjon.getRevisjon());*/
        Avtale avtaleRevisjon = innloggetNavAnsatt.opprettAvtale(opprettAvtaleRevisjon, sisteVersjonAvtaleId, opprettAvtaleRevisjon.getBaseAvtaleId(), opprettAvtaleRevisjon.getRevisjon());
        Avtale opprettetAvtaleRevisjon = avtaleRepository.save(avtaleRevisjon);
        Avtalepart avtalepart = innloggetNavAnsatt.avtalepart(opprettetAvtaleRevisjon);
        //avtalepart.endreAvtale(versjon,sisteVersjonAvtale);
        avtalepart.fylleUtAvtaleRevisjonVerdier(sisteAvtaleVersjon.getRevisjon(), sisteAvtaleVersjon, sisteAvtaleVersjon.getBaseAvtaleId() != null ? sisteAvtaleVersjon.getBaseAvtaleId(): sisteVersjonAvtaleId);
        avtaleRepository.save(opprettetAvtaleRevisjon);
        URI uri = lagUri("/avtaler/" + opprettetAvtaleRevisjon.getId());
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{avtaleId}")
    @Transactional
    public ResponseEntity endreAvtale(@PathVariable("avtaleId") UUID avtaleId,
                                      @RequestHeader("If-Match") Integer versjon,
                                      @RequestBody EndreAvtale endreAvtale) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.endreAvtale(versjon, endreAvtale);
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().header("eTag", lagretAvtale.getVersjon().toString()).build();
    }

    @GetMapping(value = "/{avtaleId}/rolle")
    public ResponseEntity<Avtalerolle> hentRolle(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        return ResponseEntity.ok(avtalepart.rolle());
    }

    @PostMapping(value = "/{avtaleId}/opphev-godkjenninger")
    @Transactional
    public ResponseEntity opphevGodkjenninger(@PathVariable("avtaleId") UUID avtaleId) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.opphevGodkjenninger();
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/godkjenn")
    @Transactional
    public ResponseEntity godkjenn(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader("If-Match") Integer versjon) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennAvtale(versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/godkjenn-paa-vegne-av")
    @Transactional
    public ResponseEntity godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn, @RequestHeader("If-Match") Integer versjon) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennPaVegneAvDeltaker(paVegneAvGrunn, versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/avbryt")
    public ResponseEntity avbryt(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader("If-Match") Integer versjon) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        //kan erstattes når det blir tilgang til Navenhet tilgangsstyring
        innloggetNavAnsatt.sjekkTilgang(avtale);
        Veileder veileder = innloggetNavAnsatt.avtalepart(avtale);
        veileder.avbrytAvtale(versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }
}
