package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.SmsProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class LagSmsFraAvtaleHendelse {
    private final SmsRepository smsRepository;
    private final SmsProducer smsProducer;

    private static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");

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
    @EventListener
    public void avtaleGodkjentAvDeltaker(GodkjentAvDeltaker event) {
        var sms = smsTilVeileder(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_DELTAKER);
        lagreOgSendKafkaMelding(sms);
    }
    @EventListener
    public void avtaleGodkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        var sms = smsTilVeileder(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER);
        lagreOgSendKafkaMelding(sms);
    }
    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), VarslbarHendelseType.AVTALE_INNGÅTT);
        var smsTilArbeidsgiver = smsTilArbeidsgiver(event.getAvtale(), VarslbarHendelseType.AVTALE_INNGÅTT);
        lagreOgSendKafkaMelding(smsTilDeltaker);
        lagreOgSendKafkaMelding(smsTilArbeidsgiver);
    }
    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        if (event.getGamleVerdier().isGodkjentAvDeltaker()) {
            var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER);
            lagreOgSendKafkaMelding(smsTilDeltaker);
        }
        var smsTilVeileder = smsTilVeileder(event.getAvtale(), VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER);
        lagreOgSendKafkaMelding(smsTilVeileder);
    }
    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        if (event.getGamleVerdier().isGodkjentAvDeltaker()) {
            var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
            lagreOgSendKafkaMelding(smsTilDeltaker);
        }
        if (event.getGamleVerdier().isGodkjentAvArbeidsgiver()) {
            var smsTilArbeidsgiver = smsTilArbeidsgiver(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
            lagreOgSendKafkaMelding(smsTilArbeidsgiver);
        }
    }
    @EventListener
    public void refusjonKlar(RefusjonKlar event) {
        String smsTekst = String.format("Dere kan nå søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Frist for å søke er om to måneder. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
        refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, VarslbarHendelseType.REFUSJON_KLAR);
    }
    @EventListener
    public void refusjonKlarRevarsel(RefusjonKlarRevarsel event) {
        String smsTekst = String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
        refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, VarslbarHendelseType.REFUSJON_KLAR_REVARSEL);
    }
    @EventListener
    public void refusjonFristForlenget(RefusjonFristForlenget event) {
        String smsTekst = String.format("Fristen for å godkjenne refusjon for avtale med nr: %s har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
        refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, VarslbarHendelseType.REFUSJON_FRIST_FORLENGET);

    }
    @EventListener
    public void refusjonKorrigert(RefusjonKorrigert event) {
        String smsTekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
        refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, VarslbarHendelseType.REFUSJON_KORRIGERT);
    }

    private void refusjonVarslingMedKontaktperson(Avtale avtale, String smsTekst, VarslbarHendelseType varslbarHendelseType) {
        if (avtale.getGjeldendeInnhold().getRefusjonKontaktperson() != null && avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf() != null) {
            if (avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getØnskerVarslingOmRefusjon()) {
                Sms smsTilArbeidsgiver = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), smsTekst, varslbarHendelseType, avtale.getId());
                lagreOgSendKafkaMelding(smsTilArbeidsgiver);
            }
            Sms smsTilKontaktpersonForRefusjon = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), avtale.getBedriftNr(), smsTekst, varslbarHendelseType, avtale.getId());
            lagreOgSendKafkaMelding(smsTilKontaktpersonForRefusjon);
        } else {
            Sms sms = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), smsTekst, varslbarHendelseType, avtale.getId());
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

    private static Sms smsTilVeileder(Avtale avtale, VarslbarHendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getVeilederTlf(), NAV_ORGNR, "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }
}
