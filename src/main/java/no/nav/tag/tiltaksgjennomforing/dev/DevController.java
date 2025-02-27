package no.nav.tag.tiltaksgjennomforing.dev;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddsperiodeConfig;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@Profile({Miljø.DEV_FSS, Miljø.LOCAL, Miljø.DOCKER_COMPOSE})
@RestController
@RequestMapping("/dev-api/")
// Controlleren er begrenset til testmiljøer, men som en ekstra sikring tillater vi kun
// at veiledere utfører disse handlingene.
@ProtectedWithClaims(issuer = "aad")
public class DevController {

    private final String miljo;
    private final AvtaleRepository avtaleRepository;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final InnloggingService innloggingService;

    public DevController(@Value("${MILJO:}") String miljo, AvtaleRepository avtaleRepository, TilskuddsperiodeConfig tilskuddsperiodeConfig, InnloggingService innloggingService) {
        this.miljo = miljo;
        this.avtaleRepository = avtaleRepository;
        this.tilskuddsperiodeConfig = tilskuddsperiodeConfig;
        this.innloggingService = innloggingService;
    }

    @PutMapping("godkjenn/{avtaleNr}")
    public ResponseEntity<String> godkjennAvtale(@PathVariable("avtaleNr") Integer avtaleNr, @RequestBody Godkjenning godkjenning) {
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
            avtale.godkjennTilskuddsperiode(godkjenning.beslutterIdent(), godkjenning.kostnadssted());
        }
        avtaleRepository.save(avtale);
        return ResponseEntity.ok().build();
    }

    @PutMapping("familierelasjon/{avtaleNr}")
    public void familierelasjon(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        EndreAvtale endring = EndreAvtale.fraAvtale(avtale);
        endring.setFamilietilknytningForklaring(null);
        endring.setHarFamilietilknytning(false);
        avtale.endreAvtale(
                Now.instant(),
                endring,
                Avtalerolle.ARBEIDSGIVER,
                tilskuddsperiodeConfig.getTiltakstyper(),
                avtale.getDeltakerFnr()
        );
        avtaleRepository.save(avtale);
    }

    @PutMapping("etterregistrering/{avtaleNr}")
    public void etterregistrering(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        avtale.togglegodkjennEtterregistrering(hentVeileder());
        avtaleRepository.save(avtale);
    }

    @PutMapping("ny-oppfolging-dato/{avtaleNr}")
    public void nyOppfolgingDato(@PathVariable("avtaleNr") Integer avtaleNr) {
        Avtale avtale = avtaleRepository.findByAvtaleNr(avtaleNr).orElseThrow();
        if (avtale.getTiltakstype().equals(Tiltakstype.VTAO)) {
            avtale.setKreverOppfolgingFom(Now.localDate().minusDays(1));
            avtale.setOppfolgingVarselSendt(null);
            avtaleRepository.save(avtale);
        }
    }

    @PostMapping("opprett-avtale")
    public ResponseEntity<String> opprettAvtale(@RequestBody Opprettelse avtale) {
        var lagretAvtale = avtaleRepository.save(Avtale.opprett(
                avtale.getOpprett(), Avtaleopphav.VEILEDER, hentVeileder()));
        lagretAvtale.endreAvtale(Now.instant(),
                avtale.getEndre(), Avtalerolle.VEILEDER, tilskuddsperiodeConfig.getTiltakstyper());
        avtaleRepository.save(lagretAvtale);
        return ResponseEntity.ok().body("\"" + lagretAvtale.getId().toString() + "\"");
    }

    private NavIdent hentVeileder() {
        return innloggingService.hentVeileder().getNavIdent();
    }
}
