package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class VeilarbArenaClient {

    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private VeilarbArenaProperties veilarbArenaProperties;

    private Boolean sjekServiceGruppeErRiktig(Oppfølgingsstatus oppfølgingsstatus) {
        try {
            return !ServiceGruppe.servicegruppeErRiktig(oppfølgingsstatus.getServicegruppe());
        } catch (Exception exception) {
            log.error("Oppfølgingsstatus servicegruppe er tom", exception);
            return false;
        }
    }

    private Boolean erGjeldendeTiltak(Tiltakstype tiltakstype) {
        return tiltakstype == Tiltakstype.SOMMERJOBB ||
                tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
                tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD;
    }

    public Oppfølgingsstatus sjekkOgHentOppfølgingStatus(OpprettAvtale opprettAvtale) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfølgingStatus(opprettAvtale.getDeltakerFnr().asString());

        if (!Objects.equals(oppfølgingStatus.getFormidlingsgruppe(), Formidlingsgruppe.IKKE_ARBEIDSSOKER.getKode()) ||
                !Objects.equals(oppfølgingStatus.getFormidlingsgruppe(), Formidlingsgruppe.INAKTIVERT_JOBBSKIFTER.getKode())) {
            throw new FeilkodeException(Feilkode.FORMIDLINGSGRUPPE_IKKE_RETTIGHET);
        }
        if (erGjeldendeTiltak(opprettAvtale.getTiltakstype()) && sjekServiceGruppeErRiktig(oppfølgingStatus)) {
            throw new FeilkodeException(Feilkode.SERVICEKODE_MANGLER);
        }
        return oppfølgingStatus;
    }

    public String hentFormidlingsgruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getFormidlingsgruppe();
    }

    public String hentServicegruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getServicegruppe().getServicekode();
    }

    public String hentOppfølgingsEnhet(String fnr) {
        Oppfølgingsstatus oppfølgingsstatus = hentOppfølgingStatus(fnr);
        if (oppfølgingsstatus != null) {
            return oppfølgingsstatus.getOppfolgingsenhet();
        }
        return null;
    }

    public Oppfølgingsstatus hentOppfølgingStatus(String fnr) {
        String uri = UriComponentsBuilder.fromHttpUrl(veilarbArenaProperties.getUrl().toString())
                .pathSegment(fnr)
                .toUriString();
        try {
            ResponseEntity<Oppfølgingsstatus> respons = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpHeadere(),
                    Oppfølgingsstatus.class
            );
            return respons.getBody();
        } catch (RestClientResponseException exception) {
            if (exception.getRawStatusCode() == HttpStatus.NOT_FOUND.value() && !exception.getResponseBodyAsString().isEmpty()) {
                log.warn("Kandidat ikke registrert i veilarbarena");
                return null;
            }
            log.error("Kunne ikke hente Oppfølgingsstatus fra veilarbarena: status=" + exception.getRawStatusCode(), exception);
            return null;
        }
    }

    private HttpEntity<String> httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(headers);
    }
}
