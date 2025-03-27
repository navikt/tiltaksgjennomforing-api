package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static no.nav.tag.tiltaksgjennomforing.TestDataGenerator.genererAvtaler;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile(Miljø.MASSE_TESTDATA)
public class LastInnMasseTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (avtaleRepository.count() != 0) {
            return;
        }
        log.info("Laster inn masse testdata");
        avtaleRepository.saveAll(genererAvtaler(490));
    }

}
