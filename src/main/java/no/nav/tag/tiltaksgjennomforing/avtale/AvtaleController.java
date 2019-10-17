package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang.TilgangUnderPilotering;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
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

    @GetMapping("/{avtaleId}/kanLaasesOpp")
    public boolean kanLaasesOpp(@PathVariable("avtaleId") UUID id) {
        for (Avtale avtale : avtaleRepository.findAll()) {
            InnloggetNavAnsatt veileder = innloggingService.hentInnloggetNavAnsatt();
            veileder.sjekkLeseTilgang(avtale);
            if ((!avtale.erGodkjentAvVeileder()) && avtale.getBaseAvtaleId().equals(id)) {
                return false;
            }
        }
      /*  for (Avtale avtale : avtaleRepository.findAll()) {
            if (avtale.getId().equals(id)) {
                return false;
            }
        }*/
        return true;
    }

    @GetMapping("/{avtaleId}/hentSisteLaastOppVersjon")
    public Avtale hentSisteLaastOppVersjon(@PathVariable("avtaleId") UUID id) {
        for (Avtale avtale : avtaleRepository.findAll()) {
            InnloggetNavAnsatt veileder = innloggingService.hentInnloggetNavAnsatt();
            veileder.sjekkLeseTilgang(avtale);
            if ((!avtale.erGodkjentAvVeileder()) && avtale.getBaseAvtaleId().equals(id)) {
                return avtale;
            }
        }
        for (Avtale avtale : avtaleRepository.findAll()) {
            if (avtale.getId().equals(id)) {
                return avtale;
            }
        }
        return avtaleRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
    }

    @PostMapping
    @Transactional
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

    @PostMapping("/opprettAvtaleGodkjentVersjon/{avtaleId}")
    @Transactional
    public ResponseEntity opprettAvtaleGodkjentVersjon(@RequestBody OpprettAvtale opprettAvtaleGodkjentVersjon,
                                                @PathVariable("avtaleId") UUID sisteVersjonAvtaleId
            /*, @RequestBody int versjon, @RequestBody UUID baseAvtaleId*/) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        tilgangUnderPilotering.sjekkTilgang(innloggetNavAnsatt.getIdentifikator());
     /*   if (kanLaasesOpp(sisteVersjonAvtaleId).getId() != sisteVersjonAvtaleId) {
            throw new TiltaksgjennomforingException("Du kan ikke låse opp/opprette en ny versjon av den avtalen. Avtalen er allerede låst opp. Sjekk avtale versjoner! " + kanLaasesOpp(sisteVersjonAvtaleId).getId());
        }*/
        Avtale sisteAvtaleVersjon = avtaleRepository.findById(sisteVersjonAvtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetNavAnsatt.sjekkSkriveTilgang(sisteAvtaleVersjon);

       /* Avtale avtaleGodkjentVersjon = innloggetNavAnsatt.opprettAvtale(opprettAvtaleGodkjentVersjon, sisteVersjonAvtaleId,
                sisteAvtaleVersjon.getBaseAvtaleId(), sisteAvtaleVersjon.getGodkjentVersjon());*/
        Veileder veileder = innloggetNavAnsatt.avtalepart(sisteAvtaleVersjon);

        Avtale avtaleGodkjentVersjon = veileder.nyAvtaleGodkjentVersjon();
        Avtale opprettetAvtaleGodkjentVersjon = avtaleRepository.save(avtaleGodkjentVersjon);
        URI uri = lagUri("/avtaler/" + opprettetAvtaleGodkjentVersjon.getId());
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public ResponseEntity godkjennPaVegneAv(@PathVariable("avtaleId") UUID avtaleId, @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn, @RequestHeader("If-Match") Integer versjon) {
        InnloggetBruker innloggetBruker = innloggingService.hentInnloggetBruker();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetBruker.sjekkSkriveTilgang(avtale);
        Avtalepart avtalepart = innloggetBruker.avtalepart(avtale);
        avtalepart.godkjennPaVegneAvDeltaker(paVegneAvGrunn, versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{avtaleId}/avbryt")
    public ResponseEntity avbryt(@PathVariable("avtaleId") UUID avtaleId, @RequestHeader("If-Match") Integer versjon) {
        InnloggetNavAnsatt innloggetNavAnsatt = innloggingService.hentInnloggetNavAnsatt();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        innloggetNavAnsatt.sjekkSkriveTilgang(avtale);
        Veileder veileder = innloggetNavAnsatt.avtalepart(avtale);
        veileder.avbrytAvtale(versjon);
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }
}
