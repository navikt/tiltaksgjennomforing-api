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

@Slf4j
@Component
@AllArgsConstructor
public class VeilarbArenaClient {

    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private VeilarbArenaProperties veilarbArenaProperties;

    private Boolean sjekServiceGruppeErRiktig(Oppfølgingsstatus oppfølgingsstatus){
        try{
            return  !oppfølgingsstatus.getServicegruppe().contains("BFORM") ||
                    !oppfølgingsstatus.getServicegruppe().contains("BATT");
        }
        catch(Exception exception) {
            log.error("Oppfølgingsstatus servicegruppe er tom", exception);
            return false;
        }
    }

    public Oppfølgingsstatus sjekkOgHentOppfølgingStatus(OpprettAvtale opprettAvtale){
        Oppfølgingsstatus oppfølgingStatus = hentOppfølgingStatus(opprettAvtale.getDeltakerFnr().asString());
        if(oppfølgingStatus.getFormidlingsgruppe() != "IARBS" || oppfølgingStatus.getFormidlingsgruppe() != "IJOBS" ){
            throw new FeilkodeException(Feilkode.FORMIDLINGSGRUPPE_IKKE_RETTIGHET);
        }
        if((opprettAvtale.getTiltakstype() == Tiltakstype.SOMMERJOBB ||
                opprettAvtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
                opprettAvtale.getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD) &&
                sjekServiceGruppeErRiktig(oppfølgingStatus)) {
                throw new FeilkodeException(Feilkode.SERVICEKODE_MANGLER);
        }
        return oppfølgingStatus;
    }

    public String hentFormidlingsgruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getFormidlingsgruppe();
    }

    public String hentServicegruppe(String fnr) {
        return hentOppfølgingStatus(fnr).getServicegruppe();
    }

    public String hentOppfølgingsEnhet(String fnr) {
        Oppfølgingsstatus oppfølgingsstatus = hentOppfølgingStatus(fnr);
        if(oppfølgingsstatus != null){
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

    private HttpEntity httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(headers);
    }
}
