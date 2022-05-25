package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DvhMeldingFilter {
    private final DvhMeldingProperties dvhMeldingFeatureProperties;

    public boolean skalTilDatavarehus(Avtale avtale) {
        if (!erFeatureSkruddPå()) {
            log.info("Feature arbeidsgiver.tiltaksgjennomforing-api.dvh-melding er ikke skrudd på, sender ingen melding til datavarehus");
            return false;
        }
        return avtale.erAvtaleInngått() && dvhMeldingFeatureProperties.getTiltakstyper().contains(avtale.getTiltakstype());
    }

    public boolean erFeatureSkruddPå() {
        return dvhMeldingFeatureProperties.isFeatureEnabled();
    }
}