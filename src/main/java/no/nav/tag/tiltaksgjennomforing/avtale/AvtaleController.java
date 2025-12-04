package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.dokgen.DokgenService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditLogging;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.EventType;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.Utfall;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.persondata.aktsomhet.Aktsomhet;
import no.nav.tag.tiltaksgjennomforing.persondata.aktsomhet.AktsomhetService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.lagUri;

@Protected
@RestController
@RequestMapping("/avtaler")
@Timed
@Slf4j
@RequiredArgsConstructor
public class AvtaleController {

    private final AvtaleRepository avtaleRepository;
    private final AvtaleInnholdRepository avtaleInnholdRepository;
    private final ArenaRyddeAvtaleRepository arenaRyddeAvtaleRepository;
    private final InnloggingService innloggingService;
    private final Norg2Client norg2Client;
    private final KontoregisterService kontoregisterService;
    private final DokgenService dokgenService;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final FilterSokRepository filterSokRepository;
    private final MeterRegistry meterRegistry;
    private final AktsomhetService aktsomhetService;

    @AuditLogging("Hent detaljer for avtale om arbeidsmarkedstiltak")
    @GetMapping("/{avtaleId}")
    public Avtale hent(@PathVariable("avtaleId") UUID id, @CookieValue("innlogget-part") Avtalerolle innloggetPart, @RequestHeader(value = "referer", required = false) final String referer) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        sendMetrikkPåPage(referer);
        return avtalepart.hentAvtale(avtaleRepository, id);
    }

    private void sendMetrikkPåPage(String referer) {
        if (StringUtils.isNotEmpty(referer)) {
            DistributionSummary summary = DistributionSummary
                    .builder("tiltaksgjennomforing.avtale.page")
                    .description("Fra side i søket avtalen åpnes")
                    .publishPercentiles(0.5, 0.75, 0.9, 0.99)
                    .register(meterRegistry);
            try {
                URL refererUrl = new URL(referer);
                String queryStr = refererUrl.getQuery();
                if (StringUtils.isNotBlank(queryStr)) {
                    String[] params = queryStr.split("&");
                    for (String param : params) {
                        String[] paramValues = param.split("=");
                        if (paramValues[0].equals("page") && paramValues.length > 1) {
                            summary.record(Double.parseDouble(paramValues[1]));
                        }
                    }
                }
            } catch (MalformedURLException e) {
            }
        }
    }

    @AuditLogging("Hent detaljer for avtale om arbeidsmarkedstiltak")
    @GetMapping("/avtaleNr/{avtaleNr}")
    public Avtale hentFraAvtaleNr(@PathVariable("avtaleNr") int avtaleNr, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        return avtalepart.hentAvtaleFraAvtaleNr(avtaleRepository, avtaleNr);
    }

    @GetMapping("/{avtaleId}/versjoner")
    public List<AvtaleInnhold> hentVersjoner(
            @PathVariable("avtaleId") UUID id,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        return innloggingService
                .hentAvtalepart(innloggetPart)
                .hentAvtaleVersjoner(
                        avtaleRepository,
                        avtaleInnholdRepository,
                        id
                );
    }

    @GetMapping
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    public PageableAvtalelisteResponse<BegrensetAvtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil(
            AvtaleQueryParameter queryParametre,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = Avtale.Fields.sistEndret) String sorteringskolonne,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Pageable pageable = PageRequest.of(
            Math.abs(page),
            Math.abs(size),
            AvtaleSorterer.getSortingOrder(avtalepart.rolle(), sorteringskolonne, "ASC")
        );
        Page<BegrensetAvtale> avtaler = avtalepart.hentBegrensedeAvtalerMedLesetilgang(avtaleRepository, queryParametre, pageable);
        return PageableAvtalelisteResponse.fra(avtaler);
    }

    @AuditLogging(value = "Oppslag på arbeidsmarkedstiltak", utfallSomLogges = Utfall.FEIL)
    @GetMapping("/sok")
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    public PageableAvtalelisteResponse<BegrensetAvtale> hentAlleAvtalerInnloggetBrukerHarTilgangTilMedGet(
            @RequestParam(value = "sokId") String filterSokId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = Avtale.Fields.sistEndret) String sorteringskolonne,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "sorteringOrder", required = false, defaultValue = "DESC") String sorteringOrder
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);

        FilterSok filterSok = filterSokRepository.findFilterSokBySokId(filterSokId).orElse(null);

        if (filterSok == null) {
            return PageableAvtalelisteResponse.tom();
        }

        filterSok.setAntallGangerSokt(filterSok.getAntallGangerSokt() + 1);
        filterSok.setSistSoktTidspunkt(Now.instant());
        filterSokRepository.save(filterSok);
        AvtaleQueryParameter avtalePredicate = filterSok.getAvtalePredicate();

        Pageable pageable = PageRequest.of(
            Math.abs(page),
            Math.abs(size),
            AvtaleSorterer.getSortingOrder(avtalepart.rolle(), sorteringskolonne, sorteringOrder)
        );
        Page<BegrensetAvtale> avtaler = avtalepart.hentBegrensedeAvtalerMedLesetilgang(
            avtaleRepository,
            avtalePredicate,
            pageable
        );

        return PageableAvtalelisteResponse.fra(
            avtaler,
            avtalePredicate,
            sorteringskolonne,
            sorteringOrder,
            filterSok.getSokId()
        );
    }

    @AuditLogging(value = "Oppslag på arbeidsmarkedstiltak", utfallSomLogges = Utfall.FEIL)
    @PostMapping("/sok")
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    public PageableAvtalelisteResponse<BegrensetAvtale> hentAlleAvtalerInnloggetBrukerHarTilgangTilMedPost(
            @RequestBody AvtaleQueryParameter queryParametre,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = Avtale.Fields.sistEndret) String sorteringskolonne,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "sorteringOrder", required = false, defaultValue = "DESC") String sorteringOrder
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Pageable pageable = PageRequest.of(
            Math.abs(page),
            Math.abs(size),
            AvtaleSorterer.getSortingOrder(avtalepart.rolle(), sorteringskolonne, sorteringOrder)
        );
        Page<BegrensetAvtale> avtaler = avtalepart.hentBegrensedeAvtalerMedLesetilgang(
                avtaleRepository,
                queryParametre,
                pageable
        );

        FilterSok filterSokiDb = filterSokRepository.findFilterSokBySokId(queryParametre.generateHash()).orElse(null);
        if (filterSokiDb != null) {
            filterSokiDb.setAntallGangerSokt(filterSokiDb.getAntallGangerSokt() + 1);
            filterSokiDb.setSistSoktTidspunkt(Now.instant());
            filterSokRepository.save(filterSokiDb);
            if (!filterSokiDb.erLik(queryParametre)) {
                log.error("Kollisjon i søkId: {}", filterSokiDb.getSokId());
            }
            return PageableAvtalelisteResponse.fra(
                avtaler,
                queryParametre,
                sorteringskolonne,
                sorteringOrder,
                filterSokiDb.getSokId()
            );
        } else {
            FilterSok filterSok = new FilterSok(queryParametre);
            filterSokRepository.save(filterSok);
            return PageableAvtalelisteResponse.fra(
                avtaler,
                queryParametre,
                sorteringskolonne,
                sorteringOrder,
                filterSok.getSokId()
            );
        }
    }

    @GetMapping("/beslutter-liste")
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    public PageableAvtalelisteResponse<BegrensetBeslutterAvtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
            AvtaleQueryParameter queryParametre,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = "startDato") String sorteringskolonne,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sorteringOrder", required = false, defaultValue = "DESC") String sorteringOrder
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Page<BegrensetBeslutterAvtale> avtaler = beslutter.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
                avtaleRepository,
                queryParametre,
                sorteringskolonne,
                page,
                size,
                sorteringOrder
        );
        return PageableAvtalelisteResponse.fra(avtaler);
    }

    @GetMapping("/{avtaleId}/pdf")
    public HttpEntity<?> hentAvtalePdf(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtale avtale = innloggingService.hentAvtalepart(innloggetPart).hentAvtale(avtaleRepository, avtaleId);
        if (!avtale.erGodkjentAvVeileder()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LASTE_NED_PDF);
        }
        byte[] avtalePdf = dokgenService.avtalePdf(avtale, innloggetPart);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_PDF);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Avtale om " + avtale.getTiltakstype().getBeskrivelse() + ".pdf");
        header.setContentLength(avtalePdf.length);
        return new HttpEntity<>(avtalePdf, header);
    }

    @PutMapping("/{avtaleId}")
    @Transactional
    public ResponseEntity<?> endreAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreAvtale endreAvtale,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);

//        if (avtale.getOpphav() == Avtaleopphav.ARENA && avtale.getTiltakstype() == Tiltakstype.MENTOR){
//            avtalepart.endreAvtale(endreAvtale, avtale, () -> avtalepart.endreMentorFnrArenaMigrertAvtale(avtale, endreAvtale.));
//        } else {
//            avtalepart.endreAvtale(endreAvtale, avtale);
//        }
//
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @AuditLogging("Test endring av avtale om arbeidsmarkedstiltak")
    @PutMapping("/{avtaleId}/dry-run")
    public Avtale endreAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreAvtale endreAvtale,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.endreAvtale(endreAvtale, avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/opphev-godkjenninger")
    @Transactional
    public void opphevGodkjenninger(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn")
    @Transactional
    public void godkjenn(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.godkjennAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping({ "/{avtaleId}/mentorGodkjennTaushetserklæring", "/{avtaleId}/godkjenn-taushetserklaering" })
    @Transactional
    public void mentorGodkjennTaushetserklæring(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        if (!avtalepart.rolle().equals(Avtalerolle.MENTOR)) {
            throw new TiltaksgjennomforingException("Du må være mentor for å signere her");
        }
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.godkjennAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    // Arbeidsgiver-operasjoner

    @GetMapping("/min-side-arbeidsgiver")
    public List<Avtale> hentAlleAvtalerForMinSideArbeidsgiver(@RequestParam("bedriftNr") BedriftNr bedriftNr) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        return arbeidsgiver.hentAvtalerForMinSideArbeidsgiver(avtaleRepository, bedriftNr);
    }

    @GetMapping(path = "/{avtaleId}/kontonummer-arbeidsgiver")
    public String hentBedriftKontonummer(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtalepart.hentAvtale(avtaleRepository, avtaleId);
        return kontoregisterService.hentKontonummer(avtale.getBedriftNr().asString());
    }

    @PostMapping("/opprett-som-arbeidsgiver")
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomArbeidsgiver(@RequestBody OpprettAvtale opprettAvtale) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        Avtale avtale = arbeidsgiver.opprettAvtale(opprettAvtale);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    /**
     * VEILEDER-OPERASJONER
     **/
    @GetMapping("/deltaker-allerede-paa-tiltak")
    @Transactional
    public ResponseEntity<List<AlleredeRegistrertAvtale>> sjekkOmDeltakerAlleredeErRegistrertPaaTiltak(
            @RequestParam(value = "deltakerFnr") Fnr deltakerFnr,
            @RequestParam(value = "tiltakstype") Tiltakstype tiltakstype,
            @RequestParam(value = "startDato", required = false) String startDato,
            @RequestParam(value = "sluttDato", required = false) String sluttDato,
            @RequestParam(value = "avtaleId", required = false) String avtaleId

    ) {
        Veileder veileder = innloggingService.hentVeileder();
        List<AlleredeRegistrertAvtale> avtaler = veileder.hentAvtaleDeltakerAlleredeErRegistrertPaa(
                deltakerFnr,
                tiltakstype,
                avtaleId != null ? UUID.fromString(avtaleId) : null,
                startDato != null ? LocalDate.parse(startDato) : null,
                sluttDato != null ? LocalDate.parse(sluttDato) : null,
                avtaleRepository
        );
        return ResponseEntity.ok(avtaler);
    }

    /**
     * VEILEDER-OPERASJONER
     **/
    @PostMapping("/deltaker-allerede-paa-tiltak")
    @Transactional
    public ResponseEntity<List<AlleredeRegistrertAvtale>> sjekkOmDeltakerAlleredeErRegistrertPaaTiltak(
            @RequestBody AlleredePaaTiltakRequest alleredePaaTiltakRequest

    ) {
        Veileder veileder = innloggingService.hentVeileder();
        List<AlleredeRegistrertAvtale> avtaler = veileder.hentAvtaleDeltakerAlleredeErRegistrertPaa(
                alleredePaaTiltakRequest.deltakerFnr(),
                alleredePaaTiltakRequest.tiltakstype(),
                alleredePaaTiltakRequest.avtaleId() != null ? UUID.fromString(alleredePaaTiltakRequest.avtaleId()) : null,
                alleredePaaTiltakRequest.startDato() != null ? LocalDate.parse(alleredePaaTiltakRequest.startDato()) : null,
                alleredePaaTiltakRequest.sluttDato() != null ? LocalDate.parse(alleredePaaTiltakRequest.sluttDato()) : null,
                avtaleRepository
        );
        return ResponseEntity.ok(avtaler);
    }

    @PostMapping
    @Transactional
    @AuditLogging(value = "Opprett avtale om arbeidsmarkedstiltak", type = EventType.CREATE, utfallSomLogges = Utfall.FEIL)
    public ResponseEntity<?> opprettAvtaleSomVeileder(@RequestBody OpprettAvtale opprettAvtale) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @AuditLogging(value = "Opprett avtale om arbeidsmarkedstiltak", type = EventType.CREATE, utfallSomLogges = Utfall.FEIL)
    @PostMapping("/opprett-mentor-avtale")
    @Transactional
    public ResponseEntity<?> opprettMentorAvtale(@RequestBody OpprettMentorAvtale opprettMentorAvtale) {
        Avtale avtale = null;
        if (opprettMentorAvtale.getDeltakerFnr().equals(opprettMentorAvtale.getMentorFnr())) {
            throw new FeilkodeException(Feilkode.DELTAGER_OG_MENTOR_KAN_IKKE_HA_SAMME_FØDSELSNUMMER);
        }
        if (opprettMentorAvtale.getAvtalerolle().equals(Avtalerolle.VEILEDER)) {
            avtale = innloggingService.hentVeileder().opprettAvtale(opprettMentorAvtale);

        } else if (opprettMentorAvtale.getAvtalerolle().equals(Avtalerolle.ARBEIDSGIVER)) {
            avtale = innloggingService.hentArbeidsgiver().opprettAvtale(opprettMentorAvtale);
        }
        if (avtale == null) {
            throw new RuntimeException("Opprett Mentor fant ingen avtale å behandle.");
        }
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{avtaleId}/forkort")
    @Transactional
    public void forkortAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody ForkortAvtale forkortAvtale,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), ForkortetGrunn.av(forkortAvtale.getGrunn(), forkortAvtale.getAnnetGrunn()));
        avtaleRepository.save(avtale);
    }

    @AuditLogging("Test forkortelse av avtale om arbeidsmarkedstiltak")
    @PostMapping("/{avtaleId}/forkort-dry-run")
    public Avtale forkortAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody ForkortAvtale forkortAvtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), ForkortetGrunn.av("dry run", ""));
        return avtale;
    }

    @PostMapping("/{avtaleId}/forleng")
    @Transactional
    public void forlengAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody ForlengAvtale forlengAvtale,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(forlengAvtale.getSluttDato(), avtale);
        avtaleRepository.save(avtale);
    }

    @AuditLogging("Test forlengelse av avtale om arbeidsmarkedstiltak")
    @PostMapping("/{avtaleId}/forleng-dry-run")
    public Avtale forlengAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody ForlengAvtale forlengAvtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(forlengAvtale.getSluttDato(), avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/endre-maal")
    @Transactional
    public void endreMål(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreMål endreMål,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreMål(endreMål, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-inkluderingstilskudd")
    @Transactional
    public void endreInkluderingstilskudd(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreInkluderingstilskudd endreInkluderingstilskudd,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreInkluderingstilskudd(endreInkluderingstilskudd, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-stillingbeskrivelse")
    @Transactional
    public void endreStillingbeskrivelse(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreStillingsbeskrivelse endreStillingsbeskrivelse,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreStillingbeskrivelse(endreStillingsbeskrivelse, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-oppfolging-og-tilrettelegging")
    @Transactional
    public void endreOppfølgingOgTilrettelegging(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-om-mentor")
    @Transactional
    public void endreOmMentor(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreOmMentor endreOmMentor,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreOmMentor(endreOmMentor, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-kontaktinfo")
    @Transactional
    public void endreKontaktinfo(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreKontaktInformasjon endreKontaktInformasjon,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreKontaktinfo(endreKontaktInformasjon, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/oppfolging-av-avtale")
    @Transactional
    public void oppfolgingAvAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.oppfolgingAvAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning")
    @Transactional
    public void endreTilskuddsberegning(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreTilskuddsberegning endreTilskuddsberegning,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        avtaleRepository.save(avtale);
    }

    @AuditLogging("Test endring av tilskuddsberegning på avtale om arbeidsmarkedstiltak")
    @PostMapping("/{avtaleId}/endre-tilskuddsberegning-dry-run")
    public Avtale endreTilskuddsberegningDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreTilskuddsberegning endreTilskuddsberegning
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/send-tilbake-til-beslutter")
    @Transactional
    public void reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av")
    @Transactional
    public void godkjennPaVegneAv(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltaker(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvArbeidsgiver(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvGrunn,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-deltaker-og-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvDeltakerOgArbeidsgiver(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvGrunn,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltakerOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/annuller")
    @Transactional
    public Avtale annuller(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody AnnullertInfo annullertInfo,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.annullerAvtale(annullertInfo.getAnnullertGrunn(), avtale);
        avtaleRepository.save(avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/del-med-avtalepart")
    @Transactional
    public void delAvtaleMedAvtalepart(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestBody Avtalerolle avtalerolle,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.delAvtaleMedAvtalepart(avtalerolle, avtale);
        avtaleRepository.save(avtale);
    }

    @PutMapping("/{avtaleId}/overta")
    @Transactional
    public void settNyVeilederPåAvtale(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.overtaAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/juster-arena-migreringsdato")
    @Transactional
    public void justerArenaMigreringsdato(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestBody JusterArenaMigreringsdato justerArenaMigreringsdato,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_ARENA_MIGRERINGSDATO_INNGAATT_AVTALE);
        }
        veileder.sjekkTilgang(avtale);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(justerArenaMigreringsdato.getMigreringsdato());
        Optional<ArenaRyddeAvtale> lagretAvtaleSomRyddeAvtale = arenaRyddeAvtaleRepository.findByAvtale(avtale);

        if (lagretAvtaleSomRyddeAvtale.isEmpty()) {
            ArenaRyddeAvtale arenaRyddeAvtale = new ArenaRyddeAvtale();
            arenaRyddeAvtale.setAvtale(avtale);
            arenaRyddeAvtale.setMigreringsdato(justerArenaMigreringsdato.getMigreringsdato());
            arenaRyddeAvtaleRepository.save(arenaRyddeAvtale);
        } else {
            ArenaRyddeAvtale oppdatertRyddeAvtale = lagretAvtaleSomRyddeAvtale.get();
            oppdatertRyddeAvtale.setMigreringsdato(justerArenaMigreringsdato.getMigreringsdato());
            arenaRyddeAvtaleRepository.save(oppdatertRyddeAvtale);
        }
        avtaleRepository.save(avtale);
    }

    @AuditLogging("Justering av migreringsdato i avtale om arbeidsmarkedstiltak")
    @PostMapping("/{avtaleId}/juster-arena-migreringsdato/dry-run")
    public Avtale justerArenaMigreringsdatoDryRun(@PathVariable("avtaleId") UUID avtaleId, @RequestBody JusterArenaMigreringsdato justerArenaMigreringsdato) {
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        Veileder veileder = innloggingService.hentVeileder();
        veileder.sjekkTilgang(avtale);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(justerArenaMigreringsdato.getMigreringsdato());
        return avtale;
    }

    @PostMapping("/{avtaleId}/godkjenn-tilskuddsperiode")
    @Transactional
    public void godkjennTilskuddsperiode(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjennTilskuddsperiodeRequest godkjennTilskuddsperiodeRequest,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        beslutter.godkjennTilskuddsperiode(avtale, godkjennTilskuddsperiodeRequest.getEnhet());
        avtaleRepository.save(avtale);
    }

    @AuditLogging(value = "Oppdater avtale om arbeidsmarkedstiltak", type = EventType.UPDATE)
    @PostMapping("/{avtaleId}/set-om-avtalen-kan-etterregistreres")
    @Transactional
    public Avtale setOmAvtalenKanEtterregistreres(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        return avtaleRepository.save(avtale);
    }

    @AuditLogging(value = "Oppdater avtale om arbeidsmarkedstiltak", type = EventType.UPDATE)
    @PostMapping("/{avtaleId}/endre-kostnadssted")
    @Transactional
    public Avtale endreKostnadssted(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreKostnadsstedRequest endreKostnadsstedRequest,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.oppdatereKostnadssted(avtale, norg2Client, endreKostnadsstedRequest.getEnhet());
        return avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/avslag-tilskuddsperiode")
    @Transactional
    public void avslåTilskuddsperiode(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestBody AvslagRequest avslagRequest,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        avtale.sjekkSistEndret(sistEndret);
        beslutter.avslåTilskuddsperiode(avtale, avslagRequest.getAvslagsårsaker(), avslagRequest.getAvslagsforklaring());
        avtaleRepository.save(avtale);
    }

    @AuditLogging(value = "Oppdater avtale om arbeidsmarkedstiltak", type = EventType.UPDATE)
    @PostMapping({"/{avtaleId}/oppdaterOppfølgingsEnhet", "/{avtaleId}/oppdater-oppfolgingsenhet"})
    public Avtale oppdaterOppfølgingsEnhet(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);
        veileder.oppdaterOppfølgingOgGeoEnhetEtterForespørsel(avtale);
        return avtaleRepository.save(avtale);
    }

    @GetMapping("/{avtaleId}/krever-aktsomhet")
    public Aktsomhet kreverAktsomhet(
        @PathVariable("avtaleId") UUID avtaleId,
        @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        return aktsomhetService.kreverAktsomhet(innloggetPart, avtaleId);
    }

    private Function<Avtale, Avtale> sjekkeSistEndret(Instant sistEndret) {
        return (avtale) -> {
            avtale.sjekkSistEndret(sistEndret);
            return avtale;
        };
    }

    @PutMapping("/{avtaleId}/kid-og-kontonummer")
    @Transactional
    public Avtale endreKidOgKontonummer(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestBody EndreKidOgKontonummer endreKidOgKontonummer,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);

        var hentNyttKontonummer = !Objects.equals(
            endreKidOgKontonummer.getArbeidsgiverKontonummer(),
            avtale.getGjeldendeInnhold().getArbeidsgiverKontonummer()
        );

        var kontonummer = hentNyttKontonummer
            ? kontoregisterService.hentKontonummer(avtale.getBedriftNr().asString())
            : avtale.getGjeldendeInnhold().getArbeidsgiverKontonummer();

        var kid = endreKidOgKontonummer.getArbeidsgiverKid();

        veileder.endreKidOgKontonummer(
            new EndreKidOgKontonummer(kontonummer, kid),
            avtale
        );
        return avtaleRepository.save(avtale);
    }

    @PatchMapping("/{avtaleId}/oppdater-mentor-fnr")
    public void oppdaterMentorFnr(
        @PathVariable("avtaleId") UUID avtaleId,
        @RequestBody String nyttMentorFnr,
        @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret
    ) {
        Fnr mentorFnr = new Fnr(nyttMentorFnr);
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .map(sjekkeSistEndret(sistEndret))
            .orElseThrow(RessursFinnesIkkeException::new);

        veileder.oppdaterMentorFnrForMigrertAvtale(mentorFnr, avtale);
        avtaleRepository.save(avtale);
    }
}
