package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.UtviklerTilgangProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/utvikler-admin/arena")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "aad")
@Unprotected
@Slf4j
public class InternalArenaMigreringController {

    private final AvtaleRepository avtaleRepository;

    private final UtviklerTilgangProperties utviklerTilgangProperties;
    private final TokenUtils tokenUtils;

    private void sjekkTilgang() {
        if (!tokenUtils.harAdGruppe(utviklerTilgangProperties.getGruppeTilgang())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/lag-tilskuddsperioder-for-en-avtale/{avtaleId}/{migreringsDato}")
    @Transactional
    public void lagTilskuddsperioderPåEnAvtale(
            @PathVariable("avtaleId") UUID id,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato,
            @RequestParam(value = "nyeTilskuddsperioderKunFremTilMigreringsdato", required = false, defaultValue = "false") boolean nyeTilskuddsperioderKunFremTilMigreringsdato) {
        sjekkTilgang();
        log.info("Lager tilskuddsperioder på en enkelt avtale {} fra dato {}", id, migreringsDato);
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(migreringsDato, false, nyeTilskuddsperioderKunFremTilMigreringsdato);
        avtaleRepository.save(avtale);
    }

}
