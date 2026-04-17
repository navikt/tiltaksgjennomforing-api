package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.altinn-tilgangsstyring")
public class AltinnTilgangsstyringProperties {
    private URI uri;
    private URI proxyUri;
    private URI arbeidsgiverAltinnTilgangerUri;
    private String altinnApiKey;
    private String apiGwApiKey;
    private String beOmRettighetBaseUrl;
    // Altinn 2 enkeltrettigheter
    private Integer ltsMidlertidigServiceCode;
    private Integer ltsMidlertidigServiceEdition;
    private Integer ltsVarigServiceCode;
    private Integer ltsVarigServiceEdition;
    private Integer arbtreningServiceCode;
    private Integer arbtreningServiceEdition;
    private Integer sommerjobbServiceCode;
    private Integer sommerjobbServiceEdition;
    private Integer inkluderingstilskuddServiceCode;
    private Integer inkluderingstilskuddServiceEdition;
    private Integer mentorServiceCode;
    private Integer mentorServiceEdition;
    private Integer vtaoServiceCode;
    private Integer vtaoServiceEdition;
    private Integer adressesperreServiceCode;
    private Integer adressesperreServiceEdition;
    private Integer ltsFirearigServiceCode;
    private Integer ltsFirearigServiceEdition;
    // Altinn 3 ressurser
    private static final String ARBEIDSTRENING = "nav_tiltak_arbeidstrening";
    private static final String MIDLERTIDIG_LONNSTILSKUDD = "nav_tiltak_midlertidig-lonnstilskudd";
    private static final String VARIG_LONNSTILSKUDD = "nav_tiltak_varig-lonnstilskudd";
    private static final String SOMMERJOBB = "nav_tiltak_sommerjobb";
    private static final String MENTOR = "nav_tiltak_mentor";
    private static final String INKLUDERINGSTILSKUDD = "nav_tiltak_inkluderingstilskudd";
    private static final String VTAO = "nav_tiltak_varig-tilrettelagt-arbeid-ordinaer";
    static final String ADRESSESPERRE = "nav_tiltak_adressesperre";
    private static final String FIREARIG_LONNSTILSKUDD = "nav_tiltak_firearig-lonnstilskudd";

    public Map<String, Tiltakstype> tilgangerTilTiltakstype() {
        Map<String, Tiltakstype> map = new HashMap<>();
        // Altinn 2 service codes
        map.put(arbtreningServiceCode + ":" + arbtreningServiceEdition, Tiltakstype.ARBEIDSTRENING);
        map.put(ltsMidlertidigServiceCode + ":" + ltsMidlertidigServiceEdition, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        map.put(ltsVarigServiceCode + ":" + ltsVarigServiceEdition, Tiltakstype.VARIG_LONNSTILSKUDD);
        map.put(sommerjobbServiceCode + ":" + sommerjobbServiceEdition, Tiltakstype.SOMMERJOBB);
        map.put(mentorServiceCode + ":" + mentorServiceEdition, Tiltakstype.MENTOR);
        map.put(inkluderingstilskuddServiceCode + ":" + inkluderingstilskuddServiceEdition, Tiltakstype.INKLUDERINGSTILSKUDD);
        map.put(vtaoServiceCode + ":" + vtaoServiceEdition, Tiltakstype.VTAO);
        // Altinn 3 ressurser
        map.put(ARBEIDSTRENING, Tiltakstype.ARBEIDSTRENING);
        map.put(MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        map.put(VARIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD);
        map.put(SOMMERJOBB, Tiltakstype.SOMMERJOBB);
        map.put(MENTOR, Tiltakstype.MENTOR);
        map.put(INKLUDERINGSTILSKUDD, Tiltakstype.INKLUDERINGSTILSKUDD);
        map.put(VTAO, Tiltakstype.VTAO);
        map.put(FIREARIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD);
        return map;
    }
}
