package no.nav.tag.tiltaksgjennomforing.avtale.service.gjeldendetilskuddsperiode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeJobbService {
    private static final int SIDESTØRRELSE = 500;
    private final GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService;

    public GjeldendeTilskuddsperiodeJobbService(GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService) {
        this.gjeldendeTilskuddsperiodeService = gjeldendeTilskuddsperiodeService;
    }

    @Async
    public void startAsynkront() {
        start();
    }

    public void start() {
        log.info("Jobb for å oppdatere gjeldendeTilskuddsperiode-felt startet...");

        Pageable side = PageRequest.of(0, SIDESTØRRELSE);
        UUID sisteId = new UUID(0L, 0L);
        boolean harFlere = true;
        int antallOppdatert = 0;
        int antallIkkeOppdatert = 0;

        while (harFlere) {
            SettGjeldendeTilskuddsperiodeRespons respons =
                gjeldendeTilskuddsperiodeService.settGjeldendeTilskuddsperiode(sisteId, side);
            sisteId = respons.sisteId();
            harFlere = respons.harFlere();
            antallOppdatert += respons.antallOppdatert();
            antallIkkeOppdatert += respons.antallIkkeOppdatert();
        }

        log.info(
            "Jobb for å oppdatere gjeldedeTilskuddsperiode-felt fullført! " +
            "Behandlet {} avtaler: {} fikk ny periode. {} hadde korrekt periode.",
            (antallOppdatert + antallIkkeOppdatert),
            antallOppdatert == 0 ? "ingen" : antallOppdatert,
            antallIkkeOppdatert == 0 ? "ingen" : antallIkkeOppdatert
        );
    }
}
