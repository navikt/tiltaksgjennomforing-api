package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleDeltMedAvtalepart;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.SmsProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Profile({ Miljø.DEV_FSS, "dockercompose" })
public class LagSmsFraAvtaleHendelse {
    private final SmsRepository smsRepository;
    private final SmsProducer smsProducer;

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        if (event.getAvtalepart() == Avtalerolle.ARBEIDSGIVER) {
            var sms = smsTilArbeidsgiver(event.getAvtale(), VarslbarHendelseType.DELT_MED_ARBEIDSGIVER);
            lagreOgSendKafkaMelding(sms);
        } else if (event.getAvtalepart() == Avtalerolle.DELTAKER) {
            var sms = smsTilDeltaker(event.getAvtale(), VarslbarHendelseType.DELT_MED_DELTAKER);
            lagreOgSendKafkaMelding(sms);
        }
    }

    private void lagreOgSendKafkaMelding(Sms sms) {
        smsRepository.save(sms);
        smsProducer.sendSmsVarselMeldingTilKafka(sms);
    }

    private static Sms smsTilDeltaker(Avtale avtale, VarslbarHendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getDeltakerTlf(), avtale.getDeltakerFnr(), "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }

    private static Sms smsTilArbeidsgiver(Avtale avtale, VarslbarHendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }
}
