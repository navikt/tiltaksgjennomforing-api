package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import java.net.URI;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.altinn-tilgangsstyring")
public class AltinnTilgangsstyringProperties {
    private URI arbeidsgiverAltinnTilgangerUri;
    private String beOmRettighetBaseUrl;

    // Altinn 2 service codes/editions — brukes fortsatt av fager-notifikasjons-API for å identifisere mottakere
    private Integer arbtreningServiceCode;
    private Integer arbtreningServiceEdition;
    private Integer ltsMidlertidigServiceCode;
    private Integer ltsMidlertidigServiceEdition;
    private Integer ltsVarigServiceCode;
    private Integer ltsVarigServiceEdition;
    private Integer sommerjobbServiceCode;
    private Integer sommerjobbServiceEdition;
    private Integer mentorServiceCode;
    private Integer mentorServiceEdition;
    private Integer inkluderingstilskuddServiceCode;
    private Integer inkluderingstilskuddServiceEdition;
    private Integer vtaoServiceCode;
    private Integer vtaoServiceEdition;
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
    private static final String FIREARIG_LONNSTILSKUDD = "nav_tiltak_firearig-lonnstilskudd";
    static final String ADRESSESPERRE = "nav_tiltak_adressesperre";

    public Map<String, Tiltakstype> tilgangerTilTiltakstype() {
        Map<String, Tiltakstype> map = new java.util.HashMap<>();
        map.put(ARBEIDSTRENING, Tiltakstype.ARBEIDSTRENING);
        map.put(MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        map.put(VARIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD);
        map.put(SOMMERJOBB, Tiltakstype.SOMMERJOBB);
        map.put(MENTOR, Tiltakstype.MENTOR);
        map.put(INKLUDERINGSTILSKUDD, Tiltakstype.INKLUDERINGSTILSKUDD);
        map.put(VTAO, Tiltakstype.VTAO);
        map.put(FIREARIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD);
        // Fager-API-en kan også returnere Altinn 2 service codes på formen "<code>:<edition>".
        // Disse mappes for å gi fortsatt funksjonalitet inntil alle ressurser er migrert til Altinn 3.
        if (arbtreningServiceCode != null) map.put(arbtreningServiceCode + ":" + arbtreningServiceEdition, Tiltakstype.ARBEIDSTRENING);
        if (ltsMidlertidigServiceCode != null) map.put(ltsMidlertidigServiceCode + ":" + ltsMidlertidigServiceEdition, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        if (ltsVarigServiceCode != null) map.put(ltsVarigServiceCode + ":" + ltsVarigServiceEdition, Tiltakstype.VARIG_LONNSTILSKUDD);
        if (sommerjobbServiceCode != null) map.put(sommerjobbServiceCode + ":" + sommerjobbServiceEdition, Tiltakstype.SOMMERJOBB);
        if (mentorServiceCode != null) map.put(mentorServiceCode + ":" + mentorServiceEdition, Tiltakstype.MENTOR);
        if (inkluderingstilskuddServiceCode != null) map.put(inkluderingstilskuddServiceCode + ":" + inkluderingstilskuddServiceEdition, Tiltakstype.INKLUDERINGSTILSKUDD);
        if (vtaoServiceCode != null) map.put(vtaoServiceCode + ":" + vtaoServiceEdition, Tiltakstype.VTAO);
        if (ltsFirearigServiceCode != null) map.put(ltsFirearigServiceCode + ":" + ltsFirearigServiceEdition, Tiltakstype.FIREARIG_LONNSTILSKUDD);
        return map;
    }
}
