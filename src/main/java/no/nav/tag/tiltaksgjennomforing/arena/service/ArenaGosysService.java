package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.oppgave.LagGosysVarselLytter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ArenaGosysService {

    private final LagGosysVarselLytter gosysVarselLytter;
    private final AvtaleRepository avtaleRepository;

    public ArenaGosysService(
        LagGosysVarselLytter gosysVarselLytter,
        AvtaleRepository avtaleRepository
    ) {
        this.gosysVarselLytter = gosysVarselLytter;
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void cleanUp() {
        log.info("Sender varsler til Gosys for {}", GosysVarselListe.avtaleIder.size());

        for (String avtaleId : GosysVarselListe.avtaleIder) {
            log.info(
                "Sender varsel til Gosys for avtaleId: {}",
                avtaleId
            );
            avtaleRepository
                .findById(UUID.fromString(avtaleId))
                .ifPresent(gosysVarselLytter::varsleGosysOmOpprettetAvtale);
        }
    }

}
