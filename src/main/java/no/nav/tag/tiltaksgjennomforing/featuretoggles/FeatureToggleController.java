package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.variant.Variant;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Unprotected
@RequestMapping("/feature")
public class FeatureToggleController {
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping
    public Map<String, Boolean> feature(@RequestParam("feature") List<String> features) {
        return featureToggleService.hentFeatureToggles(features);
    }

    @GetMapping("/variant")
    public Map<String, Variant> variant(@RequestParam("feature") List<String> features) {
        return  featureToggleService.hentVarianter(features);
    }

}
