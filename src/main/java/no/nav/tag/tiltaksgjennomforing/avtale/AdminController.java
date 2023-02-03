package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Unprotected
@RestController
@RequestMapping("/internal/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final AvtaleRepository avtaleRepository;

    @PostMapping("reberegn")
    public void reberegnLønnstilskudd(@RequestBody List<UUID> avtaleIder) {
        for (UUID avtaleId : avtaleIder) {
            Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow();
            avtale.reberegnLønnstilskudd();
            avtaleRepository.save(avtale);
        }
    }

    @PostMapping("/reberegn-mangler-dato-for-redusert-prosent/{migreringsDato}")
    @Transactional
    public void reberegnVarigLønnstilskuddSomIkkeHarRedusertDato(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        // 1. Generer dato for redusert prosent og sumRedusert
        List<Avtale> varigeLønnstilskudd = avtaleRepository.findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype.VARIG_LONNSTILSKUDD);
        log.info("Fant {} varige lønnstilskudd avtaler som mangler redusert prosent til fiksing.", varigeLønnstilskudd.size());
        varigeLønnstilskudd.forEach(avtale -> {
            if (avtale.getGjeldendeInnhold().getLonnstilskuddProsent() > 67) {
                avtale.reUtregnRedusert();
                avtale.nyeTilskuddsperioderEtterMigreringFraArena(migreringsDato, false);
                avtaleRepository.save(avtale);
            }
        });
    }

    @PostMapping("/reberegn-mangler-dato-for-redusert-prosent-dry-run/{migreringsDato}")
    public void reberegnVarigLønnstilskuddSomIkkeHarRedusertDatoDryRun(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        // 1. Generer dato for redusert prosent og sumRedusert
        List<Avtale> varigeLønnstilskudd = avtaleRepository.findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype.VARIG_LONNSTILSKUDD);
        log.info("Fant {} varige lønnstilskudd avtaler som mangler redusert prosent til fiksing.", varigeLønnstilskudd.size());
        AtomicInteger antallUnder67 = new AtomicInteger();
        varigeLønnstilskudd.forEach(avtale -> {
            if (avtale.getGjeldendeInnhold().getLonnstilskuddProsent() > 67) {
                antallUnder67.getAndIncrement();
            }
        });
        log.info("Fant {} avtaler som vil bli kjørt fiksing av redusert sum og sats på", antallUnder67.get());
    }


}
