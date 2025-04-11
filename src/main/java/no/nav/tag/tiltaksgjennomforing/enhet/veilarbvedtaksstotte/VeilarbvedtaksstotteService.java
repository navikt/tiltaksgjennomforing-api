package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.logging.SecureLog;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VeilarbvedtaksstotteService {
    private static final SecureLog secureLog = SecureLog.getLogger(log);

    private final VeilarbvedtaksstotteClient veilarbvedtaksstotteClient;

    public VeilarbvedtaksstotteService(VeilarbvedtaksstotteClient veilarbvedtaksstotteClient) {
        this.veilarbvedtaksstotteClient = veilarbvedtaksstotteClient;
    }

    public Gjeldende14aVedtakResponse hentGjeldende14aVedtak(String fnr) {
        log.info("Henter gjeldende 14-a vedtak fra veilarbvedtaksstotte");
        Optional<Gjeldende14aVedtakResponse> gjeldende14aVedtakResponse = veilarbvedtaksstotteClient.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr));
        if (gjeldende14aVedtakResponse.isEmpty()) {
            log.error("Fant ikke 14-a vedtak");
            secureLog.error("Fant ikke 14-a vedtak for fnr {}", fnr);
            throw new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV);
        }
        secureLog.info("Hentet gjeldende 14-a vedtak for {}", fnr);
        return gjeldende14aVedtakResponse.get();
    }

}
