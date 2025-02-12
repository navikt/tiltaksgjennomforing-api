package no.nav.tag.tiltaksgjennomforing.datadeling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/utvikler-admin/avtale-hendelse")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@Slf4j
public class AvtaleHendelseAdminController {

    private final AvtaleHendelseService avtaleHendelseService;
    private final AvtaleRepository avtaleRepository;

    @PostMapping("send-melding-en-avtale/{avtaleId}")
    public void sendMeldingForEnAvtale(@PathVariable("avtaleId") UUID avtaleId) {
        log.info("Sender hendelsemelding for en avtale {}", avtaleId);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtaleHendelseService.sendAvtaleHendelseMeldingPåEnAvtale(avtale);
    }

    @PostMapping("dry-send-melding-alle-avtaler")
    public void drySendMeldingAlleAvtaler() {
        log.info("DRY - Sender alle avtaler som hendelsemeldinger på topic");
        avtaleHendelseService.sendAvtaleHendelseMeldingPåAlleAvtalerDRYRun();
    }

    @PostMapping("send-melding-alle-avtaler")
    public void sendMeldingAlleAvtaler() {
        log.info("Sender alle avtaler som hendelsemeldinger på topic");
        avtaleHendelseService.sendAvtaleHendelseMeldingPåAlleAvtaler();
    }

    @PostMapping("korriger-og-send-meldinger-paa-avtaler-med-feil-dato-fra-migrering")
    public void korrigerOgSendMeldingPaaAvtalerSomHarFeilDatoFraMigrering() {
        log.info("Retter opp i og sender alle avtaler som har feil dato fra migrering av arbeidstrening");
        avtaleHendelseService.korrigerOgSendMeldingPaaAvtalerSomHarFeilDatoFraMigrering();
    }
}
