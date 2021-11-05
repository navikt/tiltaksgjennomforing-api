package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
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

@Slf4j
@Component
@AllArgsConstructor
public class VeilarbArenaClient {

    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private VeilarbArenaProperties veilarbArenaProperties;

    private boolean erMidlerTidiglonnstilskuddEllerSommerjobb(Tiltakstype tiltakstype) {
        return (tiltakstype == Tiltakstype.SOMMERJOBB ||
                tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    private boolean erVariglonnstilskudd(Tiltakstype tiltakstype) {
        return tiltakstype.equals(Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    public Oppfølgingsstatus sjekkOgHentOppfølgingStatus(Avtale avtale) {
        Oppfølgingsstatus oppfølgingStatus = hentOppfølgingStatus(avtale.getDeltakerFnr().asString());

        if (Formidlingsgruppe.ugyldigFormidlingsgruppe(oppfølgingStatus.getFormidlingsgruppe())) {
            throw new FeilkodeException(Feilkode.FORMIDLINGSGRUPPE_IKKE_RETTIGHET);
        }

        if (Kvalifiseringsgruppe.ugyldigKvalifiseringsgruppe(oppfølgingStatus.getKvalifiseringsgruppe())) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET);
        }

        if (erMidlerTidiglonnstilskuddEllerSommerjobb(avtale.getTiltakstype()) &&
                !Kvalifiseringsgruppe.kvalifisererTilMidlertidiglonnstilskuddOgSommerjobb(oppfølgingStatus.getKvalifiseringsgruppe())) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_MIDLERTIDIG_LONNTILSKUDD_OG_SOMMERJOBB_FEIL);
        }

        if (erVariglonnstilskudd(avtale.getTiltakstype()) &&
                Kvalifiseringsgruppe.kvalifisererTilVariglonnstilskudd(oppfølgingStatus.getKvalifiseringsgruppe())) {
            throw new FeilkodeException(Feilkode.KVALIFISERINGSGRUPPE_VARIG_LONNTILSKUDD_FEIL);
        }

        return oppfølgingStatus;
    }

    public String hentFormidlingsgruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getFormidlingsgruppe().getKode();
    }

    public String hentServicegruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getKvalifiseringsgruppe().getKvalifiseringskode();
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
                .queryParam("fnr", fnr).toUriString();
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
