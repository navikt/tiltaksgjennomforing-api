package no.nav.tag.tiltaksgjennomforing.oppfolging;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OppfølgingVarsleJobb {

    private AvtaleRepository avtaleRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void varsleVeilederOmOppfølging() {
        LocalDate tomånederFremiTid = Now.localDate().plusMonths(2);
        List<Avtale> avtaler = avtaleRepository.findAllByKreverOppfolgingFomLessThanAndOppfolgingVarselSendtIsNull(tomånederFremiTid);

        avtaler.forEach(avtale -> {
            //TODO: Send varsel til veileder
            avtale.setOppfolgingVarselSendt(Now.instant());
        });
        avtaleRepository.saveAll(avtaler);
    }
}
