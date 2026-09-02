package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PabegynteAvtalerRyddeService {
    private final AvtaleRepository avtaleRepository;

    public PabegynteAvtalerRyddeService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public PabegynteAvtalerRyddeBatchRespons ryddBatch(UUID fraId, Limit limit, boolean ryddejobbAktivert) {
        List<UUID> ider = avtaleRepository.finnAvtaleIderSomErPabegyntEllerManglerGodkjenning(fraId, limit);
        boolean harFlere = ider.size() == limit.max();

        if (ider.isEmpty()) {
            return new PabegynteAvtalerRyddeBatchRespons(fraId, false, 0, 0, 0);
        }

        UUID sisteId = ider.getLast();

        List<Avtale> avtaler = avtaleRepository.findAllById(ider);
        Map<AvtaleUtlopHandling, List<Avtale>> avtaleHandling = avtaler.stream()
            .collect(Collectors.groupingBy(AvtaleUtlopHandling::parse));

        int antallUtlop = Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.UTLOP)).map(List::size).orElse(0);
        int antallVarsel24Timer = Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_24_TIMER)).map(List::size).orElse(0);
        int antallVarselEnUke = Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_EN_UKE)).map(List::size).orElse(0);

        if (ryddejobbAktivert) {
            avtaleHandling.forEach((handling, avtaleliste) -> {
                switch (handling) {
                    case VARSEL_EN_UKE -> avtaleliste.forEach(avtale -> {
                        avtale.utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
                        avtaleRepository.save(avtale);
                    });
                    case VARSEL_24_TIMER -> avtaleliste.forEach(avtale -> {
                        avtale.utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
                        avtaleRepository.save(avtale);
                    });
                    case UTLOP -> avtaleliste.forEach(avtale -> {
                        log.info(
                            "Utløper avtale {} med status {} som sist var endret {}",
                            avtale.getId(),
                            avtale.getStatus(),
                            avtale.getSistEndret()
                        );
                        avtale.utlop(AvtaleUtlopHandling.UTLOP);
                        avtaleRepository.save(avtale);
                    });
                }
            });
        }

        return new PabegynteAvtalerRyddeBatchRespons(sisteId, harFlere, antallUtlop, antallVarsel24Timer, antallVarselEnUke);
    }
}
