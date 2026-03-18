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
    private String arbeidstrening = "nav_tiltak_arbeidstrening";
    private String midlertidigLonnstilskudd = "nav_tiltak_midlertidig-lonnstilskudd";
    private String varigLonnstilskudd = "nav_tiltak_varig-lonnstilskudd";
    private String sommerjobb = "nav_tiltak_sommerjobb";
    private String mentor = "nav_tiltak_mentor";
    private String inkluderingstilskudd = "nav_tiltak_inkluderingstilskudd";
    private String vtao = "nav_tiltak_varig-tilrettelagt-arbeid-ordinaer";
    private String adressesperre = "nav_tiltak_adressesperre";

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
        map.put(arbeidstrening, Tiltakstype.ARBEIDSTRENING);
        map.put(midlertidigLonnstilskudd, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        map.put(varigLonnstilskudd, Tiltakstype.VARIG_LONNSTILSKUDD);
        map.put(sommerjobb, Tiltakstype.SOMMERJOBB);
        map.put(mentor, Tiltakstype.MENTOR);
        map.put(inkluderingstilskudd, Tiltakstype.INKLUDERINGSTILSKUDD);
        map.put(vtao, Tiltakstype.VTAO);
        return map;
    }
}
