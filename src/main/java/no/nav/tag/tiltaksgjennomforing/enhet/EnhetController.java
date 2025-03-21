package no.nav.tag.tiltaksgjennomforing.enhet;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@RequestMapping("/enheter")
@Timed
@Slf4j
@RequiredArgsConstructor
public class EnhetController {
    private final Norg2Client norg2Client;
    
    @GetMapping("/{enhetsnummer}")
    public ResponseEntity<HentEnhetResponse> hent(@PathVariable("enhetsnummer") String enhetsnummer) {
        Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(enhetsnummer);
        
        if (response == null || response.getStatus() == Norg2EnhetStatus.NEDLAGT) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
         
        return ResponseEntity.ok(HentEnhetResponseMapper.map(response));
    }
}
