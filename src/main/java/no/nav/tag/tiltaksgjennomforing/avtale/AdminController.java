package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/reberegn-mangler-dato-for-redusert-prosent")
    public void reberegnLønnstilskuddSomIkkeHarRedusertDato() {
        // 1. Generer dato for redusert prosent
        List<Avtale> varigeLønnstilskudd = avtaleRepository.findAllByGjeldendeInnhold_SumLønnstilskuddRedusertNullAndGjeldendeInnhold_AvtaleInngåttNotNullAndTiltakstype(Tiltakstype.VARIG_LONNSTILSKUDD);
        varigeLønnstilskudd.forEach(avtale -> {
            avtale.reberegnLønnstilskudd();
            avtaleRepository.save(avtale);
        });

        // 2. Lag tilskuddsperioder på nytt. Må sjekke at ingen er godkjent osv. Sørge for at redusert sats blir brukt.
    }
}
