package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.RequiredArgsConstructor;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/be-om-altinn-rettighet-urler")
@Unprotected
@RequiredArgsConstructor
public class BeOmAltinnRettighetUrlerController {
    private final AltinnTilgangsstyringProperties props;

    @GetMapping
    public Map<Tiltakstype, String> beOmRettighetUrler(@RequestParam("orgNr") String orgNr) {
        return Map.of(
                Tiltakstype.ARBEIDSTRENING, beOmRettighetUrl(orgNr, props.getArbtreningServiceCode(), props.getArbtreningServiceEdition()),
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, beOmRettighetUrl(orgNr, props.getLtsMidlertidigServiceCode(), props.getLtsMidlertidigServiceEdition()),
                Tiltakstype.VARIG_LONNSTILSKUDD, beOmRettighetUrl(orgNr, props.getLtsVarigServiceCode(), props.getLtsVarigServiceEdition())
        );
    }

    private String beOmRettighetUrl(String orgNr, Integer serviceCode, Integer serviceEdition) {
        return props.getBeOmRettighetBaseUrl() + "&bedrift=" + orgNr;
    }
}