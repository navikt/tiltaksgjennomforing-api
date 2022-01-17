package no.nav.tag.tiltaksgjennomforing.varsel;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.SOMMERJOBB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;

@Slf4j
public class SmsVarselFactory {
    public static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";

    private final Avtale avtale;
    private final VarslbarHendelse hendelse;

    public SmsVarselFactory(Avtale avtale, VarslbarHendelse hendelse) {
        this.avtale = avtale;
        this.hendelse = hendelse;
    }

    public SmsVarsel deltaker() {
        return SmsVarsel.nyttVarsel(avtale.getGjeldendeInnhold().getDeltakerTlf(), avtale.getDeltakerFnr(), SELVBETJENINGSONE_VARSELTEKST, hendelse.getId());
    }

    public SmsVarsel arbeidsgiver() {
        return SmsVarsel.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(), SELVBETJENINGSONE_VARSELTEKST, hendelse.getId());
    }

    public List<SmsVarsel> arbeidsgiverRefusjonKlar() {
        erSommerjobbAvtale();
        String smsTekst = String.format("Dere kan nå søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Frist for å søke er om to måneder. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        return hentSMSVarselForValgtePersoner(avtale,  smsTekst, hendelse);
    }

    public List<SmsVarsel> arbeidsgiverRefusjonKlarRevarsel() {
        erSommerjobbAvtale();
        String smsTekst = String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        return hentSMSVarselForValgtePersoner(avtale, smsTekst, hendelse);
    }

    public List<SmsVarsel> arbeidsgiverRefusjonForlengetVarsel() {
        erSommerjobbAvtale();
        String smsTekst = String.format("Fristen for å godkjenne refusjon for avtale med nr: %s har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        return hentSMSVarselForValgtePersoner(avtale,  smsTekst, hendelse);
    }

    private void erSommerjobbAvtale() {
        if(!avtale.getTiltakstype().equals(SOMMERJOBB)) throw new RuntimeException();
    }

    public List<SmsVarsel> arbeidsgiverRefusjonKorrigertVarsel() {
        erSommerjobbAvtale();
        String smsTekst = String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtale.getAvtaleNr());
        return hentSMSVarselForValgtePersoner(avtale,  smsTekst, hendelse);
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getGjeldendeInnhold().getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST, hendelse.getId());
    }

    private List<SmsVarsel> hentSMSVarselForValgtePersoner(Avtale avtale, String smsTekst, VarslbarHendelse hendelse) {
        log.info("Sender SMS til {} getRefusjonKontaktperson", avtale.getGjeldendeInnhold().getRefusjonKontaktperson());
        ArrayList<SmsVarsel> smsVarsel = new ArrayList<>(Arrays.asList(SmsVarsel.nyttVarsel(avtale.getGjeldendeInnhold().getArbeidsgiverTlf(), avtale.getBedriftNr(),
            smsTekst, hendelse.getId())));
        if(avtale.getGjeldendeInnhold().getRefusjonKontaktperson() != null && avtale.getGjeldendeInnhold().getRefusjonKontaktperson().erIkkeTom()) {
            log.info("SMS VARSEL Ønsker info om refusjon: {} ", avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getØnskerInformasjonOmRefusjon());
            if(!avtale.getGjeldendeInnhold().getRefusjonKontaktperson().getØnskerInformasjonOmRefusjon()) {
                smsVarsel.clear();
            }
            smsVarsel.add(SmsVarsel.nyttVarselForGjeldendeKontaktpersonForRefusjon(avtale, smsTekst, hendelse.getId()));
        }
        log.info("SMS VARSEL {} {}", smsVarsel.size(), smsVarsel);
        return smsVarsel;

    }
}
