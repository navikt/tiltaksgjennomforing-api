package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class GosysVarselService {
    static final String GOSYS_OPPRETTET_AVTALE_BESKRIVELSE = "Avtale er opprettet av arbeidsgiver på tiltak %s. Se avtalen under filteret 'Ufordelte' i https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing";
    static final String GOSYS_AVTALE_HENTET_FRA_ARENA = "Avtale hentet fra Arena på tiltak %s. Se avtalen her: https://tiltaksgjennomforing.intern.nav.no/tiltaksgjennomforing/avtale/%s";
    static final String VTAO_INNGÅTT = "Brukeren har fått innvilget tiltaksplass og har startet på varig tilrettelagt arbeid i ordinær virksomhet %s.";

    private static final DateTimeFormatter NORSK_DATO = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag("nb-NO"));

    private final OppgaveVarselService oppgaveVarselService;
    private final PersondataService persondataService;

    void varsleGosysOmOpprettetAvtale(Avtale avtale) {
        final AktorId aktørid = persondataService
            .hentGjeldendeAktørId(avtale.getDeltakerFnr())
            .orElseThrow(() -> new IllegalStateException("Kan ikke opprette gosys-varsel uten aktørId"));
        Tiltakstype tiltakstype = avtale.getTiltakstype();

        String beskrivelse;
        if (Avtaleopphav.ARENA == avtale.getOpphav()) {
            beskrivelse = format(GOSYS_AVTALE_HENTET_FRA_ARENA, tiltakstype.getBeskrivelse(), avtale.getId());
        } else {
            beskrivelse = format(GOSYS_OPPRETTET_AVTALE_BESKRIVELSE, tiltakstype.getBeskrivelse());
        }
        try {
            oppgaveVarselService.opprettOppgave(new OppgaveRequest(
                aktørid,
                GosysTema.TILTAK,
                GosysBehandlingstype.SOKNAD,
                tiltakstype,
                beskrivelse,
                Optional.ofNullable(avtale.getEnhetOppfolging()).orElse(avtale.getEnhetGeografisk())
            ));
            log.info("Opprettet gosys-oppgave for 'avtale opprettet' (avtaleid = {})", avtale.getId());
        } catch (Exception e) {
            log.error("Klarte ikke opprette oppgave for 'avtale opprettet' (avtaleid = {})", avtale.getId(), e);
            throw e;
        }
    }

    void varsleGosysOmInngaattVTAOAvtale(Avtale avtale) {
        final AktorId aktørid = persondataService
            .hentGjeldendeAktørId(avtale.getDeltakerFnr())
            .orElseThrow(() -> new IllegalStateException("Kan ikke opprette gosys-varsel uten aktørId"));

        try {
            oppgaveVarselService.opprettOppgave(new OppgaveRequest(
                aktørid,
                GosysTema.UFORETRYGD,
                GosysBehandlingstype.INGEN,
                null,
                format(VTAO_INNGÅTT, NORSK_DATO.format(avtale.getGjeldendeInnhold().getStartDato())),
                null
            ));
            log.info("Opprettet gosys-oppgave for 'vtao-avtale inngått' (avtaleid = {})", avtale.getId());
        } catch (Exception e) {
            log.error("Klarte ikke opprette oppgave for 'vtao-avtale inngått' (avtaleid = {})", avtale.getId(), e);
            throw e;
        }
    }

}
