package no.nav.tag.tiltaksgjennomforing.pdfgen;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.AvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoering;
import no.nav.tag.tiltaksgjennomforing.journalfoering.AvtaleTilJournalfoeringMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PdfgenService {
    private final RestTemplate restTemplate;
    private final PdfgenProperties pdfgenProperties;
    private final MeterRegistry meterRegistry;

    public PdfgenService(
        RestTemplate noAuthRestTemplate,
        PdfgenProperties pdfgenProperties,
        MeterRegistry meterRegistry
    ) {
        this.restTemplate = noAuthRestTemplate;
        this.pdfgenProperties = pdfgenProperties;
        this.meterRegistry = meterRegistry;
    }

    public byte[] avtalePdf(AvtaleDTO avtale, Avtalerolle avtalerolle) {
        var avtaleTilJournalfoering = AvtaleTilJournalfoeringMapper.tilJournalfoering(avtale.gjeldendeInnhold(), avtale, avtalerolle);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AvtaleTilJournalfoering> request = new HttpEntity<>(avtaleTilJournalfoering, headers);
            String url = pdfgenProperties.getUri() + "/tiltak-avtale/" + avtale.tiltakstype().name().toLowerCase().replace("_", "-");
            byte[] bytes = restTemplate.postForObject(url, request, byte[].class);
            meterRegistry.counter("tiltaksgjennomforing.pdf.ok", "avtalerolle", avtalerolle.name()).increment();
            return bytes;
        } catch (RestClientException e) {
            log.error("Feil ved kall til pdfgen for henting av PDF", e);
            meterRegistry.counter("tiltaksgjennomforing.pdf.feil", "avtalerolle", avtalerolle.name()).increment();
            throw e;
        }
    }
}
