package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Veileder;
import no.nav.tag.tiltaksgjennomforing.oppfolging.OppfølgingVarsleJobb;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Profile({Miljø.DEV_FSS, Miljø.LOCAL, Miljø.DOCKER_COMPOSE})
@RestController
@RequestMapping("/dev/")
// Controlleren er begrenset til testmiljøer, men som en ekstra sikring tillater vi kun
// at veiledere utfører disse handlingene.
@ProtectedWithClaims(issuer = "aad")
public class DevController {
    private final AvtaleRepository avtaleRepository;
    private final InnloggingService innloggingService;
    private final OppfølgingVarsleJobb oppfølgingVarsleJobb;

    public DevController(AvtaleRepository avtaleRepository, InnloggingService innloggingService, OppfølgingVarsleJobb oppfølgingVarsleJobb) {
        this.avtaleRepository = avtaleRepository;
        this.innloggingService = innloggingService;
        this.oppfølgingVarsleJobb = oppfølgingVarsleJobb;
    }

    @PutMapping("/avtale/{avtaleNr}/godkjenn")
    ResponseEntity<String> godkjennAvtale(
            @PathVariable("avtaleNr") Integer avtaleNr,
            @RequestBody GodkjennAvtaleRequest godkjennAvtaleRequest
    ) {
        Optional<Avtale> kanskjeAvtale = avtaleRepository.findByAvtaleNr(avtaleNr);

        if (kanskjeAvtale.isEmpty()) {
            return ResponseEntity.badRequest().body("Avtale fins ikke");
        }
        var avtale = kanskjeAvtale.get();

        var felterSomIkkeErFyltUt = avtale.felterSomIkkeErFyltUt();
        if (!felterSomIkkeErFyltUt.isEmpty()) {
            return ResponseEntity.badRequest().body("Felter mangler: " + felterSomIkkeErFyltUt);
        }

        if (!avtale.erGodkjentAvDeltaker()) {
            avtale.godkjennForDeltaker(avtale.getDeltakerFnr());
        }
        if (!avtale.erGodkjentAvArbeidsgiver()) {
            // Deltaker er også arbeidsgiver i testmiljø!
            avtale.godkjennForArbeidsgiver(avtale.getDeltakerFnr());
        }
        if (!avtale.erGodkjentAvVeileder()) {
            avtale.godkjennForVeileder(avtale.getVeilederNavIdent());
        }
        if (!avtale.erAvtaleInngått()) {
            avtale.godkjennTilskuddsperiode(godkjennAvtaleRequest.beslutterIdent(), godkjennAvtaleRequest.kostnadssted());
        }
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/avtale/{avtaleNr}/familierelasjon")
    void familierelasjon(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        EndreAvtale endring = EndreAvtale.fraAvtale(avtale);
        endring.setFamilietilknytningForklaring(null);
        endring.setHarFamilietilknytning(false);
        avtale.endreAvtale(
                Now.instant(),
                endring,
                Avtalerolle.ARBEIDSGIVER,
                avtale.getDeltakerFnr()
        );
        avtaleRepository.save(avtale);
    }

    @PutMapping("/avtale/{avtaleNr}/etterregistrering")
    void etterregistrering(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        avtale.togglegodkjennEtterregistrering(hentVeileder());
        avtaleRepository.save(avtale);
    }

    @PutMapping("/avtale/{avtaleNr}/ny-oppfolging-dato")
    void nyOppfolgingDato(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        if (avtale.getTiltakstype().equals(Tiltakstype.VTAO)) {
            avtale.setKreverOppfolgingFom(Now.localDate().minusDays(1));
            avtale.setOppfolgingVarselSendt(null);
            avtaleRepository.save(avtale);
        }
        oppfølgingVarsleJobb.varsleVeilederOmOppfølging();
    }

    @PostMapping("/avtale")
    ResponseEntity<String> opprettAvtale(@RequestBody OpprettAvtaleRequest avtale) {
        Veileder veileder = innloggingService.hentVeileder();
        var lagretAvtale = veileder.opprettAvtale(avtale.opprett());
        veileder.endreAvtale(Now.instant(), avtale.endre(), lagretAvtale);
        avtaleRepository.save(lagretAvtale);
        return ResponseEntity.ok().body(lagretAvtale.getId().toString());
    }

    private NavIdent hentVeileder() {
        return innloggingService.hentVeileder().getNavIdent();
    }
}
