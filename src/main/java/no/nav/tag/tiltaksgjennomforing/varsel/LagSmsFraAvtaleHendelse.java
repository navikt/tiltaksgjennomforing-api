package no.nav.tag.tiltaksgjennomforing.varsel;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getunleash.UnleashContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleDeltMedAvtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonFristForlenget;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlar;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlarRevarsel;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKorrigert;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.SmsProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Slf4j
public class LagSmsFraAvtaleHendelse {
    private final SmsRepository smsRepository;
    private final SmsProducer smsProducer;
    private final FeatureToggleService featureToggleService;

    private static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        switch(event.getAvtalepart()){
            case ARBEIDSGIVER -> lagreOgSendKafkaMelding(smsTilArbeidsgiver(event.getAvtale(), HendelseType.DELT_MED_ARBEIDSGIVER));
            case DELTAKER -> lagreOgSendKafkaMelding(smsTilDeltaker(event.getAvtale(), HendelseType.DELT_MED_DELTAKER));
            case MENTOR -> lagreOgSendKafkaMelding(smsTilMentor(event.getAvtale(), HendelseType.DELT_MED_MENTOR));
        }
    }
    @EventListener
    public void avtaleGodkjentAvDeltaker(GodkjentAvDeltaker event) {
        var sms = smsTilVeileder(event.getAvtale(), HendelseType.GODKJENT_AV_DELTAKER);
        lagreOgSendKafkaMelding(sms);
    }
    @EventListener
    public void avtaleGodkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        var sms = smsTilVeileder(event.getAvtale(), HendelseType.GODKJENT_AV_ARBEIDSGIVER);
        lagreOgSendKafkaMelding(sms);
    }
    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), HendelseType.AVTALE_INNGÅTT);
        var smsTilArbeidsgiver = smsTilArbeidsgiver(event.getAvtale(), HendelseType.AVTALE_INNGÅTT);

        if(event.getAvtale().getTiltakstype() == Tiltakstype.MENTOR) {
            var smsTilMentor = smsTilMentor(event.getAvtale(), HendelseType.AVTALE_INNGÅTT);
            lagreOgSendKafkaMelding(smsTilMentor);
        }

        if (!smsMinSideToggleErPå()) {
            lagreOgSendKafkaMelding(smsTilDeltaker);
        }
        if (!smsMinSideArbeidsgiverToggleErPå()) {
            lagreOgSendKafkaMelding(smsTilArbeidsgiver);
        }

    }
    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        if (event.getGamleVerdier().isGodkjentAvDeltaker()) {
            var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), HendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER);
            if (!smsMinSideToggleErPå()) {
                lagreOgSendKafkaMelding(smsTilDeltaker);
            }
        }
        var smsTilVeileder = smsTilVeileder(event.getAvtale(), HendelseType.OPPRETTET_AV_ARBEIDSGIVER);
        lagreOgSendKafkaMelding(smsTilVeileder);
    }
    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        if (event.getGamleVerdier().isGodkjentAvDeltaker()) {
            var smsTilDeltaker = smsTilDeltaker(event.getAvtale(), HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
            if (!smsMinSideToggleErPå()) {
                lagreOgSendKafkaMelding(smsTilDeltaker);
            }
        }
        if (event.getGamleVerdier().isGodkjentAvArbeidsgiver()) {
            var smsTilArbeidsgiver = smsTilArbeidsgiver(event.getAvtale(), HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
            if (!smsMinSideArbeidsgiverToggleErPå()) {
                lagreOgSendKafkaMelding(smsTilArbeidsgiver);
            }
        }
    }
    @EventListener
    public void refusjonKlar(RefusjonKlar event) {
        if(event.getAvtale().getTiltakstype() == Tiltakstype.SOMMERJOBB || event.getAvtale().getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.MENTOR){
            String tiltakNavn = event.getAvtale().getTiltakstype().getBeskrivelse().toLowerCase();
            String smsTekst = String.format("Dere kan nå søke om refusjon for tilskudd til %s for avtale med nr: %s. Frist for å søke er %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", tiltakNavn, event.getAvtale().getAvtaleNr(), event.getFristForGodkjenning());
            refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, HendelseType.REFUSJON_KLAR);
        }
    }

    @EventListener
    public void refusjonKlarRevarsel(RefusjonKlarRevarsel event) {
        if(event.getAvtale().getTiltakstype() == Tiltakstype.SOMMERJOBB || event.getAvtale().getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.MENTOR) {
            String tiltakNavn = event.getAvtale().getTiltakstype().getBeskrivelse().toLowerCase();
            String smsTekst = String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til %s for avtale med nr: %s. Frist for å søke er %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.",tiltakNavn, event.getAvtale().getAvtaleNr(), event.getFristForGodkjenning());
            refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, HendelseType.REFUSJON_KLAR_REVARSEL);
        }
    }

    @EventListener
    public void refusjonFristForlenget(RefusjonFristForlenget event) {
        if(event.getAvtale().getTiltakstype() == Tiltakstype.SOMMERJOBB || event.getAvtale().getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.MENTOR) {
            String smsTekst = String.format("Fristen for å godkjenne refusjon for avtale med nr: %s har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
            refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, HendelseType.REFUSJON_FRIST_FORLENGET);
        }
    }

    @EventListener
    public void refusjonKorrigert(RefusjonKorrigert event) {
        if(event.getAvtale().getTiltakstype() == Tiltakstype.SOMMERJOBB || event.getAvtale().getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.VARIG_LONNSTILSKUDD || event.getAvtale().getTiltakstype() == Tiltakstype.MENTOR) {
            String smsTekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", event.getAvtale().getAvtaleNr());
            refusjonVarslingMedKontaktperson(event.getAvtale(), smsTekst, HendelseType.REFUSJON_KORRIGERT);
        }
    }

    private void refusjonVarslingMedKontaktperson(Avtale avtale, String smsTekst, HendelseType hendelseType) {
        if (avtale.getGjeldendeInnhold().getRefusjonKontaktperson() != null && avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf() != null) {
            if (avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getØnskerVarslingOmRefusjon() != null && avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getØnskerVarslingOmRefusjon()) {
                Sms smsTilArbeidsgiver = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), smsTekst, hendelseType, avtale.getId());
                lagreOgSendKafkaMelding(smsTilArbeidsgiver);
            }
            Sms smsTilKontaktpersonForRefusjon = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), avtale.getBedriftNr(), smsTekst, hendelseType, avtale.getId());
            lagreOgSendKafkaMelding(smsTilKontaktpersonForRefusjon);
        } else {
            Sms sms = Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), smsTekst, hendelseType, avtale.getId());
            lagreOgSendKafkaMelding(sms);
        }
    }

    private boolean smsMinSideToggleErPå() {
        Boolean smsMinSidetogglePå = featureToggleService.isEnabled(FeatureToggle.SMS_MIN_SIDE_DELTAKER);
        if (smsMinSidetogglePå) {
            log.info("Toggle sms-min-side-deltaker er på: sender ikke sms til deltaker");
            return true;
        } else {
            log.info("Toggle sms-min-side-deltaker er av: sender sms til deltaker som vanlig");
            return false;
        }
    }
    private boolean smsMinSideArbeidsgiverToggleErPå() {
        Boolean smsMinSideArbeidsgiverTogglePå = featureToggleService.isEnabled(FeatureToggle.ARBEIDSGIVERNOTIFIKASJON_MED_SAK_OG_SMS);
        if (smsMinSideArbeidsgiverTogglePå) {
            log.info("Toggle arbeidsgivernotifikasjon-med-sak-og-sms er på: sender ikke sms til arbeidsgiver");
            return true;
        } else {
            log.info("Toggle arbeidsgivernotifikasjon-med-sak-og-sms er av: sender sms til arbeidsgiver som vanlig");
            return false;
        }
    }
    private boolean skalSendeSmsTilTlfNr(String tlfNr) {
        UnleashContext unleashContext = UnleashContext.builder().addProperty("tlfNr", tlfNr).build();
        Boolean smsTogglePå = featureToggleService.isEnabled(FeatureToggle.SMS_TIL_MOBILNUMMER, unleashContext);
        if (smsTogglePå) {
            log.info("Toggle sms-mobil-whitelist er på: sender bare sms for white-listed nummer");
            return true;
        } else {
            log.info("Toggle sms-mobil-whitelist er av: sender sms til alle mobilnummere");
            return false;
        }
    }

    private void lagreOgSendKafkaMelding(Sms sms) {
        if (!skalSendeSmsTilTlfNr(sms.getTelefonnummer())) {
            return;
        }
        try {
            smsRepository.save(sms);
            smsProducer.sendSmsVarselMeldingTilKafka(sms);
        } catch (JsonProcessingException e) {
            log.error("Feil ved sending av sms", e);
        }

    }

    private static Sms smsTilDeltaker(Avtale avtale, HendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getDeltakerTlf(), avtale.getDeltakerFnr(), "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }

    private static Sms smsTilMentor(Avtale avtale, HendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getMentorTlf(), avtale.getMentorFnr(), "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }

    private static Sms smsTilArbeidsgiver(Avtale avtale, HendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }

    private static Sms smsTilVeileder(Avtale avtale, HendelseType hendelse) {
        return Sms.nyttVarsel(avtale.getGjeldendeInnhold().getVeilederTlf(), NAV_ORGNR, "Du har mottatt et nytt varsel på https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing", hendelse, avtale.getId());
    }
}
