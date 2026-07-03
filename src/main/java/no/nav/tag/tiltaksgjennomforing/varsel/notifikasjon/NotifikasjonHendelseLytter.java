package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlar;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.notifikasjoner.enabled")
@Slf4j
public class NotifikasjonHendelseLytter {

    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;
    private final NotifikasjonService notifikasjonService;
    private final NotifikasjonParser parser;
    private final FeatureToggleService featureToggleService;

    private void opprettOgSendNyBeskjed(Avtale avtale, HendelseType hendelseType, NotifikasjonTekst tekst) {
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(avtale,
                hendelseType, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettNyBeskjed(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                tekst);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        if (featureToggleService.isEnabled(FeatureToggle.REFUSJON_KLAR_I_TILTAK_NOTIFIKASJON)) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.REFUSJON_KLAR,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON);
    }
}
