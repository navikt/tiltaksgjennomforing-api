package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/be-om-altinn-rettighet-urler")
@Unprotected
@RequiredArgsConstructor
public class BeOmAltinnRettighetUrlerController {
    private final AltinnTilgangsstyringProperties props;

    @GetMapping
    public List<Heh> beOmRettighetUrler(@RequestParam("orgNr") String orgNr) {
        return List.of(
                new Heh(Tiltakstype.ARBEIDSTRENING, beOmRettighetUrl(orgNr, props.getArbtreningServiceCode(), props.getArbtreningServiceEdition())),
                new Heh(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, beOmRettighetUrl(orgNr, props.getLtsMidlertidigServiceCode(), props.getLtsMidlertidigServiceEdition())),
                new Heh(Tiltakstype.VARIG_LONNSTILSKUDD, beOmRettighetUrl(orgNr, props.getLtsVarigServiceCode(), props.getLtsVarigServiceEdition()))
        );
    }

    private String beOmRettighetUrl(String orgNr, Integer serviceCode, Integer serviceEdition) {
        return props.getBeOmRettighetBaseUrl() + "?offeredBy=" + orgNr + "&resource=" + serviceCode + "_" + serviceEdition;
    }

    @Value
    private static class Heh {
        Tiltakstype tiltakstype;
        String url;
    }
}
