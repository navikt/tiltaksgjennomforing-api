package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.UtviklerTilgangProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/utvikler-admin/arena")
@RequiredArgsConstructor
@ProtectedWithClaims(issuer = "aad")
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
    public void lagTilskuddsperioderPåEnAvtale(@PathVariable("avtaleId") UUID id, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        sjekkTilgang();
        log.info("Lager tilskuddsperioder på en enkelt avtale {} fra dato {}", id, migreringsDato);
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato);
    }

    @PostMapping("/lag-tilskuddsperioder-arena/{migreringsDato}")
    @Transactional
    public void lagTilskuddsperioderPåArenaAvtaler(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        sjekkTilgang();
        // Finn alle lønnstilskuddavtaler (varig og midlertidlig)
        AtomicInteger antallMigrert = new AtomicInteger();
        List<Avtale> midlertidigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        log.info("Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for midlertidig lønnstilskudd", midlertidigLønnstilskuddAvtaler.size());

        midlertidigLønnstilskuddAvtaler.forEach(avtale -> {
            if(avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato)) {
                antallMigrert.getAndIncrement();
            }
            if(antallMigrert.get() % 100 == 0) {
                log.info("Migrert {} antall avtaler", antallMigrert.get());
            }
        });

        List<Avtale> varigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.VARIG_LONNSTILSKUDD);

        log.info("Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for varig lønnstilskudd", varigLønnstilskuddAvtaler.size());

        varigLønnstilskuddAvtaler.forEach(avtale -> {
            if(avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato)) {
                antallMigrert.getAndIncrement();
            }
            if(antallMigrert.get() % 100 == 0) {
                log.info("Migrert {} antall avtaler", antallMigrert.get());
            }
        });

        log.info("Migrering av tilskuddsperioder for gamle avtaler i arena fullført. {}", antallMigrert.get());
    }
}
