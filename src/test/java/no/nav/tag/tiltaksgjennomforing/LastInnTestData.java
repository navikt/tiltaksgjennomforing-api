package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({ Miljø.TESTDATA, Miljø.DEV_GCP_LABS })
public class LastInnTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final static List<UUID> AFTER_MIGRATE_MOCK_AVTALER = List.of(
            UUID.fromString("16ba7ded-8b14-4972-9394-389d513eda91"),
            UUID.fromString("76ef4c0f-1ca4-4433-8313-09e6db8f5493"),
            UUID.fromString("4a9bd80b-9a72-41a9-8e0e-83cd53a5ffad"),
            UUID.fromString("5019be60-7604-4a2c-bae0-47b1780ab139"),
            UUID.fromString("e0619f5a-c2eb-4149-9803-f77eca4cf343"),
            UUID.fromString("206cb3fa-f1f2-4da5-ab6b-0caf847fbf1c"),
            UUID.fromString("89d671cc-0edf-464a-9e59-b5c7473242b9"),
            UUID.fromString("7950419a-8730-4ad9-8a14-b99590ff15f0")
    );

    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!isLastInnTestdata()) {
            return;
        }

        log.info("Laster testdata");

        avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederUtenTilskuddsperioder());
        avtaleRepository.save(TestData.enArbeidstreningAvtale());
        avtaleRepository.save(TestData.enMentorAvtaleSignert());
        avtaleRepository.save(TestData.enMentorAvtaleUsignert());
        avtaleRepository.save(TestData.enInkluderingstilskuddAvtale());
        avtaleRepository.save(TestData.enInkluderingstilskuddAvtaleUtfyltOgGodkjentAvArbeidsgiver());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfylt());
        avtaleRepository.save(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder());
        avtaleRepository.save(TestData.enAvtaleMedFlereVersjoner());
        avtaleRepository.save(TestData.enAvtaleKlarForOppstart());
        Avtale lilly = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        lilly.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtaleRepository.save(lilly);
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder());

        avtaleRepository.save(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid());
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        avtaleRepository.save(TestData.enSommerjobbAvtaleGodkjentAvVeileder());
        avtaleRepository.save(TestData.enSommerjobbAvtaleGodkjentAvBeslutter());
        avtaleRepository.save(TestData.enSommerjobbAvtaleGodkjentAvArbeidsgiver());
        Now.resetClock();
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder());
        avtaleRepository.save(TestData.enMentorAvtaleMedMedAltUtfylt());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaleRepository.save(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet());
        avtaleRepository.save(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtaleRepository.save(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.VARIG_LONNSTILSKUDD));
        avtaleRepository.save(TestData.enVarigLonnstilskuddAvtaleMedBehandletIArenaPerioder());
        avtaleRepository.save(TestData.enVtaoAvtaleGodkjentAvArbeidsgiver());

        Now.fixedDate(LocalDate.of(2024, 10, 1));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 7, 23));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 6));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 5));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 4));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 3));
        avtaleRepository.save(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();
    }

    private boolean isLastInnTestdata() {
        return avtaleRepository.count() == 0 || (avtaleRepository.count() == AFTER_MIGRATE_MOCK_AVTALER.size()
            && AFTER_MIGRATE_MOCK_AVTALER.stream().allMatch(avtaleRepository::existsById));
    }
}
