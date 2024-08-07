package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
@Data
@Slf4j
public class NotifikasjonParser {
    private final String nyOppgave;
    private final String nyBeskjed;
    private final String oppgaveUtfoertByEksternId;
    private final String mineNotifikasjoner;
    private final String softDeleteNotifikasjonByEksternId;
    private final String hardDeleteNotifikasjon;

    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;

    public NotifikasjonParser(
            @Value("classpath:varsler/opprettNyOppgave.graphql") Resource nyOppgave,
            @Value("classpath:varsler/opprettNyBeskjed.graphql") Resource nyBeskjed,
            @Value("classpath:varsler/oppgaveUtfoertByEksternId.graphql") Resource oppgaveUtfoertByEksternId,
            @Value("classpath:varsler/mineNotifikasjoner.graphql") Resource mineNotifikasjoner,
            @Value("classpath:varsler/softDeleteNotifikasjonByEksternId.graphql") Resource softDeleteNotifikasjonByEksternId,
            @Value("classpath:varsler/hardDeleteNotifikasjon.graphql") Resource hardDeleteNotifikasjon,
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties
    ) {
        this.nyOppgave = resourceAsString(nyOppgave);
        this.nyBeskjed = resourceAsString(nyBeskjed);
        this.oppgaveUtfoertByEksternId = resourceAsString(oppgaveUtfoertByEksternId);
        this.mineNotifikasjoner = resourceAsString(mineNotifikasjoner);
        this.softDeleteNotifikasjonByEksternId = resourceAsString(softDeleteNotifikasjonByEksternId);
        this.hardDeleteNotifikasjon = resourceAsString(hardDeleteNotifikasjon);
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
    }

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    public AltinnNotifikasjonsProperties getNotifikasjonerProperties(Avtale avtale) {
        return switch (avtale.getTiltakstype()) {
            case MIDLERTIDIG_LONNSTILSKUDD -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
                    altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition());
            case ARBEIDSTRENING -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getArbtreningServiceCode(),
                    altinnTilgangsstyringProperties.getArbtreningServiceEdition());
            case SOMMERJOBB -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getSommerjobbServiceCode(),
                    altinnTilgangsstyringProperties.getSommerjobbServiceEdition());
            case MENTOR -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getMentorServiceCode(),
                    altinnTilgangsstyringProperties.getMentorServiceEdition());
            case INKLUDERINGSTILSKUDD -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getInkluderingstilskuddServiceCode(),
                    altinnTilgangsstyringProperties.getInkluderingstilskuddServiceEdition());
            case VARIG_LONNSTILSKUDD -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                    altinnTilgangsstyringProperties.getLtsVarigServiceEdition());
            case VTAO -> new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getVtaoServiceCode(),
                    altinnTilgangsstyringProperties.getVtaoServiceEdition());
        };
    }
}
