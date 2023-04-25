package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggingService;
import no.nav.tag.tiltaksgjennomforing.dokgen.DokgenService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Map.entry;
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
    private final EregService eregService;
    private final Norg2Client norg2Client;
    private final KontoregisterService kontoregisterService;
    private final DokgenService dokgenService;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final VeilarbArenaClient veilarbArenaClient;

    @GetMapping("/{avtaleId}")
    public Avtale hent(@PathVariable("avtaleId") UUID id, @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        return avtalepart.hentAvtale(avtaleRepository, id);
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
    public List<Avtale> hentAlleAvtalerInnloggetBrukerHarTilgangTil(
            AvtalePredicate queryParametre,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = Avtale.Fields.sistEndret) String sorteringskolonne,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart,
            @RequestParam(value = "skip", required = false, defaultValue = "0") Integer skip,
            @RequestParam(value = "limit", required = false, defaultValue = "100000000") Integer limit
    ) {
        return innloggingService.hentAvtalepart(innloggetPart).hentAlleAvtalerMedLesetilgang(
                avtaleRepository,
                queryParametre,
                sorteringskolonne,
                skip,
                limit
        );
    }

    @GetMapping("/beslutter-liste")
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    public Map<String, Object> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
            AvtalePredicate queryParametre,
            @RequestParam(value = "sorteringskolonne", required = false, defaultValue = "startDato") String sorteringskolonne,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Instant start = Instant.now();
        Page<AvtaleMinimal> avtaler = beslutter.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
                avtaleRepository,
                queryParametre,
                sorteringskolonne,
                page,
                size
        );

        List<AvtaleMinimal> avtalerMedTilgang = avtaler.getContent().stream()
                .filter(avtaleMinimal -> beslutter.harTilgangTilFnr(
                        new Fnr(avtaleMinimal.getDeltakerFnr()))).toList();

        return Map.ofEntries(
                entry("avtaler", avtalerMedTilgang),
                entry("size", avtaler.getSize()),
                entry("currentPage", avtaler.getNumber()),
                entry("totalItems", avtaler.getTotalElements()),
                entry("totalPages", avtaler.getTotalPages())
        );
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
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody EndreAvtale endreAvtale,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);

        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale, tilskuddsperiodeConfig.getTiltakstyper());
        Avtale lagretAvtale = avtaleRepository.save(avtale);
        return ResponseEntity.ok().lastModified(lagretAvtale.getSistEndret()).build();
    }

    @PutMapping("/{avtaleId}/dry-run")
    public Avtale endreAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody EndreAvtale endreAvtale, @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.endreAvtale(sistEndret, endreAvtale, avtale, tilskuddsperiodeConfig.getTiltakstyper());
        return avtale;
    }

    @PostMapping("/{avtaleId}/opphev-godkjenninger")
    @Transactional
    public void opphevGodkjenninger(
            @PathVariable("avtaleId") UUID avtaleId,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.opphevGodkjenninger(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn")
    @Transactional
    public void godkjenn(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.godkjennAvtale(sistEndret, avtale);
        avtaleRepository.save(avtale);
    }
    @PostMapping("/{avtaleId}/mentorGodkjennTaushetserklæring")
    @Transactional
    public void mentorGodkjennTaushetserklæring(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @CookieValue("innlogget-part") Avtalerolle innloggetPart
    ) {
        Avtalepart avtalepart = innloggingService.hentAvtalepart(innloggetPart);
        if(!avtalepart.rolle().equals(Avtalerolle.MENTOR)) throw new TiltaksgjennomforingException("Du må være mentor for å signere her");
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtalepart.godkjennAvtale(sistEndret, avtale);
        avtaleRepository.save(avtale);
    }


    // Arbeidsgiver-operasjoner

    @GetMapping("/min-side-arbeidsgiver")
    public List<Avtale> hentAlleAvtalerForMinSideArbeidsgiver(@RequestParam("bedriftNr") BedriftNr bedriftNr) {
        Arbeidsgiver arbeidsgiver = innloggingService.hentArbeidsgiver();
        return arbeidsgiver.hentAvtalerForMinsideArbeidsgiver(avtaleRepository, bedriftNr);
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

    /** VEILEDER-OPERASJONER **/
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
        return new ResponseEntity<List<AlleredeRegistrertAvtale>>(avtaler, HttpStatus.OK);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> opprettAvtaleSomVeileder(
            @RequestBody OpprettAvtale opprettAvtale,
            @RequestParam(value = "ryddeavtale", required = false)
            Boolean ryddeavtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(avtale.getBedriftNr()).getBedriftNavn());

        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        if (ryddeavtale != null && ryddeavtale) {
            ArenaRyddeAvtale arenaRyddeAvtale = new ArenaRyddeAvtale();
            arenaRyddeAvtale.setAvtale(avtale);
            arenaRyddeAvtaleRepository.save(arenaRyddeAvtale);
        }
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/opprett-mentor-avtale")
    @Transactional
    public ResponseEntity<?> opprettMentorAvtale(@RequestBody OpprettMentorAvtale opprettMentorAvtale) {
        Avtale avtale = null;
        if(opprettMentorAvtale.getDeltakerFnr().equals(opprettMentorAvtale.getMentorFnr())){
            throw new FeilkodeException(Feilkode.DELTAGER_OG_MENTOR_KAN_IKKE_HA_SAMME_FØDSELSNUMMER);
        }

        if(opprettMentorAvtale.getAvtalerolle().equals(Avtalerolle.VEILEDER)){
            avtale = innloggingService.hentVeileder().opprettMentorAvtale(opprettMentorAvtale);

        }
        else if(opprettMentorAvtale.getAvtalerolle().equals(Avtalerolle.ARBEIDSGIVER)){
            avtale = innloggingService.hentArbeidsgiver().opprettMentorAvtale(opprettMentorAvtale);
        }
        if(avtale == null){
            throw new RuntimeException("Opprett Mentor fant ingen avtale å behandle.");
        }
        avtale.leggTilBedriftNavn(eregService.hentVirksomhet(opprettMentorAvtale.getBedriftNr()).getBedriftNavn());
        Avtale opprettetAvtale = avtaleRepository.save(avtale);
        URI uri = lagUri("/avtaler/" + opprettetAvtale.getId());
        return ResponseEntity.created(uri).build();
    }


    @PostMapping("/{avtaleId}/forkort")
    @Transactional
    public void forkortAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody ForkortAvtale forkortAvtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), forkortAvtale.getGrunn(), forkortAvtale.getAnnetGrunn());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/forkort-dry-run")
    public Avtale forkortAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody ForkortAvtale forkortAvtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        // Er ikke nødvending med en reell grunn siden det ikke påvirker tilskuddsperioder
        veileder.forkortAvtale(avtale, forkortAvtale.getSluttDato(), "dry run", "");
        return avtale;
    }

    @PostMapping("/{avtaleId}/forleng")
    @Transactional
    public void forlengAvtale(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody ForlengAvtale forlengAvtale
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.forlengAvtale(forlengAvtale.getSluttDato(), avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/forleng-dry-run")
    public Avtale forlengAvtaleDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
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
            @RequestBody EndreMål endreMål
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreMål(endreMål, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-inkluderingstilskudd")
    @Transactional
    public void endreInkluderingstilskudd(@PathVariable("avtaleId") UUID avtaleId,
                         @RequestBody EndreInkluderingstilskudd endreInkluderingstilskudd) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreInkluderingstilskudd(endreInkluderingstilskudd, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-stillingbeskrivelse")
    @Transactional
    public void endreStillingbeskrivelse(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreStillingsbeskrivelse endreStillingsbeskrivelse
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreStillingbeskrivelse(endreStillingsbeskrivelse, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-oppfolging-og-tilrettelegging")
    @Transactional
    public void endreOppfølgingOgTilrettelegging(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-om-mentor")
    @Transactional
    public void endreOmMentor(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreOmMentor endreOmMentor
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreOmMentor(endreOmMentor, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-kontaktinfo")
    @Transactional
    public void endreKontaktinfo(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreKontaktInformasjon endreKontaktInformasjon
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreKontaktinfo(endreKontaktInformasjon, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning")
    @Transactional
    public void endreTilskuddsberegning(@PathVariable("avtaleId") UUID avtaleId,
                                        @RequestBody EndreTilskuddsberegning endreTilskuddsberegning) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/endre-tilskuddsberegning-dry-run")
    public Avtale endreTilskuddsberegningDryRun(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreTilskuddsberegning endreTilskuddsberegning
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.endreTilskuddsberegning(endreTilskuddsberegning, avtale);
        return avtale;
    }

    @PostMapping("/{avtaleId}/send-tilbake-til-beslutter")
    @Transactional
    public void sendTilbakeTilBeslutter(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId)
                .orElseThrow(RessursFinnesIkkeException::new);
        veileder.sendTilbakeTilBeslutter(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping({"/{avtaleId}/godkjenn-paa-vegne-av", "/{avtaleId}/godkjenn-paa-vegne-av-deltaker"})
    @Transactional
    public void godkjennPaVegneAv(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneGrunn paVegneAvGrunn
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltaker(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvArbeidsgiver(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvGrunn
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/godkjenn-paa-vegne-av-deltaker-og-arbeidsgiver")
    @Transactional
    public void godkjennPaVegneAvDeltakerOgArbeidsgiver(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvGrunn
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.godkjennForVeilederOgDeltakerOgArbeidsgiver(paVegneAvGrunn, avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/annuller")
    @Transactional
    public void annuller(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) Instant sistEndret,
            @RequestBody AnnullertInfo annullertInfo
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.annullerAvtale(sistEndret, annullertInfo.getAnnullertGrunn(), avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/slettemerk")
    @Transactional
    public void slettemerk(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.slettemerk(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/del-med-avtalepart")
    @Transactional
    public void delAvtaleMedAvtalepart(@PathVariable("avtaleId") UUID avtaleId, @RequestBody Avtalerolle avtalerolle) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.delAvtaleMedAvtalepart(avtalerolle, avtale);
        avtaleRepository.save(avtale);
    }

    @PutMapping("/{avtaleId}/overta")
    @Transactional
    public void settNyVeilederPåAvtale(@PathVariable("avtaleId") UUID avtaleId) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.hentOppfølgingFraArena(avtale, veilarbArenaClient);
        veileder.overtaAvtale(avtale);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/juster-arena-migreringsdato")
    @Transactional
    public void justerArenaMigreringsdato(@PathVariable("avtaleId") UUID avtaleId, @RequestBody JusterArenaMigreringsdato justerArenaMigreringsdato) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_ENDRE_ARENA_MIGRERINGSDATO_INNGAATT_AVTALE);
        }
        veileder.sjekkTilgang(avtale);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(justerArenaMigreringsdato.getMigreringsdato(), false);
        Optional<ArenaRyddeAvtale> lagretAvtaleSomRyddeAvtale = arenaRyddeAvtaleRepository.findByAvtale(avtale);

        if (!lagretAvtaleSomRyddeAvtale.isPresent()) {
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

    @PostMapping("/{avtaleId}/juster-arena-migreringsdato/dry-run")
    public Avtale justerArenaMigreringsdatoDryRun(@PathVariable("avtaleId") UUID avtaleId, @RequestBody JusterArenaMigreringsdato justerArenaMigreringsdato) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.sjekkTilgang(avtale);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(justerArenaMigreringsdato.getMigreringsdato(), false);
        return avtale;
    }

    @PostMapping("/{avtaleId}/godkjenn-tilskuddsperiode")
    @Transactional
    public void godkjennTilskuddsperiode(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody GodkjennTilskuddsperiodeRequest godkjennTilskuddsperiodeRequest
    ) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.godkjennTilskuddsperiode(avtale, godkjennTilskuddsperiodeRequest.getEnhet());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/set-om-avtalen-kan-etterregistreres")
    @Transactional
    public Avtale setOmAvtalenKanEtterregistreres(@PathVariable("avtaleId") UUID avtaleId){
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        var oppdatertAvtale = avtaleRepository.save(avtale);
        return oppdatertAvtale;
    }

    @PostMapping("/{avtaleId}/endre-kostnadssted")
    @Transactional
    public Avtale endreKostnadssted(
            @PathVariable("avtaleId") UUID avtaleId,
            @RequestBody EndreKostnadsstedRequest endreKostnadsstedRequest
    ) {
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        veileder.oppdatereKostnadssted(avtale, norg2Client, endreKostnadsstedRequest.getEnhet());
        var oppdatertAvtale = avtaleRepository.save(avtale);
        return oppdatertAvtale;
    }

    @PostMapping("/{avtaleId}/avslag-tilskuddsperiode")
    @Transactional
    public void avslåTilskuddsperiode(@PathVariable("avtaleId") UUID avtaleId, @RequestBody AvslagRequest avslagRequest) {
        Beslutter beslutter = innloggingService.hentBeslutter();
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        beslutter.avslåTilskuddsperiode(avtale, avslagRequest.getAvslagsårsaker(), avslagRequest.getAvslagsforklaring());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/{avtaleId}/oppdaterOppfølgingsEnhet")
    public Avtale oppdaterOppfølgingsEnhet(
            @PathVariable("avtaleId") UUID avtaleId
    ){
        Veileder veileder = innloggingService.hentVeileder();
        Avtale avtale = veileder.hentAvtale(avtaleRepository, avtaleId);
        veileder.oppdatereEnheterEtterForespørsel(avtale);
        var oppdatertAvtale = avtaleRepository.save(avtale);

        return oppdatertAvtale;
    };


}
