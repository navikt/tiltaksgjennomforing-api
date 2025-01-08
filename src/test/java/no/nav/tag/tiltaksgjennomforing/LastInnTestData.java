package no.nav.tag.tiltaksgjennomforing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({ Miljø.TESTDATA, Miljø.DEV_GCP_LABS })
public class LastInnTestData implements ApplicationListener<ApplicationReadyEvent> {
    private final AvtaleRepository avtaleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (avtaleRepository.count() != 0) {
            return;
        }

        List<Avtale> avtaler = new ArrayList<>();

        log.info("Laster testdata");

        avtaler.add(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederUtenTilskuddsperioder());
        avtaler.add(TestData.enArbeidstreningAvtale());
        avtaler.add(TestData.enMentorAvtaleSignert());
        avtaler.add(TestData.enMentorAvtaleUsignert());
        avtaler.add(TestData.enInkluderingstilskuddAvtale());
        avtaler.add(TestData.enInkluderingstilskuddAvtaleUtfyltOgGodkjentAvArbeidsgiver());
        avtaler.add(TestData.enAvtaleMedAltUtfylt());
        avtaler.add(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder());
        avtaler.add(TestData.enAvtaleMedFlereVersjoner());
        avtaler.add(TestData.enAvtaleKlarForOppstart());
        Avtale lilly = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        lilly.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtaler.add(lilly);
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder());

        avtaler.add(TestData.enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid());
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvVeileder());
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvBeslutter());
        avtaler.add(TestData.enSommerjobbAvtaleGodkjentAvArbeidsgiver());
        Now.resetClock();
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder());
        avtaler.add(TestData.enMentorAvtaleMedMedAltUtfylt());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet());
        avtaler.add(TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet());
        avtaler.add(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtaler.add(TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.VARIG_LONNSTILSKUDD));
        avtaler.add(TestData.enVarigLonnstilskuddAvtaleMedBehandletIArenaPerioder());
        avtaler.add(TestData.enVtaoAvtaleGodkjentAvVeileder());
        avtaler.add(TestData.enVtaoAvtaleGodkjentAvArbeidsgiver());

        Now.fixedDate(LocalDate.of(2024, 10, 1));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 7, 23));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 6));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 5));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 4));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        Now.fixedDate(LocalDate.of(2024, 9, 3));
        avtaler.add(TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt());
        Now.resetClock();

        avtaler.forEach(avtale -> {
            avtale.setStatus(Status.fra(avtale));
            avtaleRepository.save(avtale);
        });
    }
}
