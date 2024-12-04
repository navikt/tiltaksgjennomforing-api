package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static java.lang.String.format;

@Slf4j
@Component
class LagGosysVarselLytter {

    private final PersondataService persondataService;
    private final Task<OppgaveRequest> opprettOppgave;
    private final Scheduler scheduler;

    private static final DateTimeFormatter NORSK_DATO = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.forLanguageTag("nb-NO"));
    protected final static String GOSYS_OPPRETTET_AVTALE_BESKRIVELSE = "Avtale er opprettet av arbeidsgiver på tiltak %s. Se avtalen under filteret 'Ufordelte' i https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing";
    protected final static String GOSYS_AVTALE_HENTET_FRA_ARENA = "Avtale hentet fra Arena på tiltak %s. Se avtalen her: https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing/avtale/%s";
    protected final static String VTAO_INNGÅTT = "Brukeren har fått innvilget tiltaksplass og har startet på varig tilrettelagt arbeid i skjermet/ordinær bedrift %s.";

    public LagGosysVarselLytter(
            PersondataService persondataService,
            Scheduler scheduler,
            Task<OppgaveRequest> opprettOppgave
    ) {
        this.persondataService = persondataService;
        this.scheduler = scheduler;
        this.opprettOppgave = opprettOppgave;
    }

    private void opprettGosysOppgaveOmOpprettetAvtale(Avtale avtale) {
        final String aktørid = persondataService.hentAktørId(avtale.getDeltakerFnr());
        Tiltakstype tiltakstype = avtale.getTiltakstype();

        String beskrivelse;
        if (Avtaleopphav.ARENA == avtale.getOpphav()) {
            beskrivelse = format(GOSYS_AVTALE_HENTET_FRA_ARENA, tiltakstype.getBeskrivelse(), avtale.getId());
        } else {
            beskrivelse = format(GOSYS_OPPRETTET_AVTALE_BESKRIVELSE, tiltakstype.getBeskrivelse());
        }
        scheduler.schedule(
                opprettOppgave.instance(format("GOSYS_AVTALE_OPPRETTET_%s", avtale.getId()),
                        new OppgaveRequest(
                                aktørid,
                                GosysTema.TILTAK,
                                GosysBehandlingstype.SOKNAD,
                                tiltakstype,
                                beskrivelse
                        )),
                Now.instant()
        );
    }

    private void opprettGosysOppgaveOmInngaattVTAOAvtale(Avtale avtale) {
        final String aktørid = persondataService.hentAktørId(avtale.getDeltakerFnr());
        scheduler.schedule(
                opprettOppgave.instance(format("GOSYS_VTAO_INNGÅTT_%s", avtale.getId()),
                        new OppgaveRequest(
                                aktørid,
                                GosysTema.UFORETRYGD,
                                GosysBehandlingstype.INGEN,
                                null,
                                format(VTAO_INNGÅTT, NORSK_DATO.format(avtale.getGjeldendeInnhold().getStartDato())))
                ), Now.instant()
        );
    }

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArbeidsgiver event) {
        opprettGosysOppgaveOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettGosysVarsel(AvtaleOpprettetAvArena event) {
        opprettGosysOppgaveOmOpprettetAvtale(event.getAvtale());
    }

    @TransactionalEventListener
    public void opprettVTAOGosysVarsel(AvtaleInngått event) {
        if (event.getAvtale().getTiltakstype().equals(Tiltakstype.VTAO)) {
            opprettGosysOppgaveOmInngaattVTAOAvtale(event.getAvtale());
        }
    }
}
