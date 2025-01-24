package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
class LagGosysVarselLytter {
    private final OppgaveVarselService oppgaveVarselService;
    private final PersondataService persondataService;

    private static final DateTimeFormatter NORSK_DATO = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.forLanguageTag("nb-NO"));

    protected final static String GOSYS_OPPRETTET_AVTALE_BESKRIVELSE = "Avtale er opprettet av arbeidsgiver på tiltak %s. Se avtalen under filteret 'Ufordelte' i https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing";
    protected final static String GOSYS_AVTALE_HENTET_FRA_ARENA = "Avtale hentet fra Arena på tiltak %s. Se avtalen her: https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing/avtale/%s";
    protected final static String VTAO_INNGÅTT = "Brukeren har fått innvilget tiltaksplass og har startet på varig tilrettelagt arbeid i skjermet/ordinær bedrift %s.";

    private void varsleGosysOmOpprettetAvtale(Avtale avtale) {
        final String aktørid = persondataService.hentAktørId(avtale.getDeltakerFnr());
        Tiltakstype tiltakstype = avtale.getTiltakstype();

        String beskrivelse;
        if (Avtaleopphav.ARENA == avtale.getOpphav()) {
            beskrivelse = format(GOSYS_AVTALE_HENTET_FRA_ARENA, tiltakstype.getBeskrivelse(), avtale.getId());
        } else {
            beskrivelse = format(GOSYS_OPPRETTET_AVTALE_BESKRIVELSE, tiltakstype.getBeskrivelse());
        }
        try {
            oppgaveVarselService.opprettOppgave(new OppgaveRequest(aktørid, GosysTema.TILTAK, GosysBehandlingstype.SOKNAD, tiltakstype, beskrivelse));
            log.info("Opprettet gosys-oppgave for 'avtale opprettet' (avtaleid = {})", avtale.getId());
        } catch (Exception e) {
            log.error("Klarte ikke opprette oppgave for 'avtale opprettet' (avtaleid = {})", avtale.getId(), e);
            throw e;
        }
    }

    private void varsleGosysOmInngaattVTAOAvtale(Avtale avtale) {
        final String aktørid = persondataService.hentAktørId(avtale.getDeltakerFnr());
        try {
            oppgaveVarselService.opprettOppgave(new OppgaveRequest(
                    aktørid,
                    GosysTema.UFORETRYGD,
                    GosysBehandlingstype.INGEN,
                    null,
                    format(VTAO_INNGÅTT, NORSK_DATO.format(avtale.getGjeldendeInnhold().getStartDato())))
            );
            log.info("Opprettet oppgave for 'vtao-avtale inngått' (avtaleid = {})", avtale.getId());
        } catch (Exception e) {
            log.error("Klarte ikke opprette oppgave for 'vtao-avtale inngått' (avtaleid = {})", avtale.getId(), e);
            throw e;
        }
    }

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArbeidsgiver event) {
        varsleGosysOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArena event) {
        varsleGosysOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettVTAOGosysVarsel(AvtaleInngått event) {
        if (event.getAvtale().getTiltakstype().equals(Tiltakstype.VTAO)) {
            varsleGosysOmInngaattVTAOAvtale(event.getAvtale());
        }
    }
}
