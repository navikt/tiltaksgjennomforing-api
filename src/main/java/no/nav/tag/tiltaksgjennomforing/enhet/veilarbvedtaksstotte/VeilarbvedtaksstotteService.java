package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class VeilarbvedtaksstotteService {
    private final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private final VeilarbvedtaksstotteClient client;

    public VeilarbvedtaksstotteService(VeilarbvedtaksstotteClient client) {
        this.client = client;
    }

    public Optional<Gjeldende14aVedtakRespons> hentGjeldende14aVedtak(Fnr fnr) {
        return hentGjeldende14aVedtak(fnr.asString());
    }

    private Optional<Gjeldende14aVedtakRespons> hentGjeldende14aVedtak(String fnr) {
        Optional<Gjeldende14aVedtakRespons> respons;

        try {
            respons = client.hentGjeldende14aVedtak(new Gjeldende14aVedtakRequest(fnr));
        } catch (Exception e) {
            log.error("Feil ved henting av gjeldende § 14 a-vedtak fra veilarbvedtaksstotte", e);
            throw new FeilkodeException(Feilkode.HENTING_AV_14A_VEDTAK_FEILET);
        }

        if (respons.isEmpty()) {
            teamLogs.info("Fant ikke gjeldende § 14 a-vedtak for fnr {}", fnr);
        }

        return respons;
    }

}
