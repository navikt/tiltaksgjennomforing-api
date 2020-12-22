package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.nav.security.oidc.api.Unprotected;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Map;

@RestController
@Unprotected
public class FeatureToggleController {
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/feature")
    public ResponseEntity<Map<String, Boolean>> feature(@RequestParam("feature") List<String> features) {
        return ResponseEntity.status(OK).body(featureToggleService.hentFeatureToggles(features));
    }

}
