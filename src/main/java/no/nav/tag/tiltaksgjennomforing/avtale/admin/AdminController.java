package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgangsattributter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.satser.Sats.VTAO_SATS;

@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@RestController
@RequestMapping("/utvikler-admin/")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final AvtaleRepository avtaleRepository;
    private final TilskuddPeriodeRepository tilskuddPeriodeRepository;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;
    private final AdminService adminService;

    @PostMapping("reberegn")
    public void reberegnLønnstilskudd(@RequestBody List<UUID> avtaleIder) {
        for (UUID avtaleId : avtaleIder) {
            Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow();
            avtale.reberegnLønnstilskudd();
            avtaleRepository.save(avtale);
        }
    }

    @PostMapping("/reberegn-mangler-dato-for-redusert-prosent/{migreringsDato}")
    @Transactional
    public void reberegnVarigLønnstilskuddSomIkkeHarRedusertDato(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        log.info("Starter jobb for å fikse manglende redusert prosent og redusert sum");
        // 1. Generer dato for redusert prosent og sumRedusert
        List<Avtale> varigeLønnstilskudd = avtaleRepository.findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(
            Tiltakstype.VARIG_LONNSTILSKUDD);
        log.info("Fant {} varige lønnstilskudd avtaler som mangler redusert prosent til fiksing.", varigeLønnstilskudd.size());
        AtomicInteger antallUnder67 = new AtomicInteger();
        varigeLønnstilskudd.forEach(avtale -> {
            LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
            LocalDate sluttDato = avtale.getGjeldendeInnhold().getSluttDato();
            if (avtale.getGjeldendeInnhold().getLonnstilskuddProsent() > 67
                    && startDato.isBefore(sluttDato.minusMonths(12))
                    && avtale.getAnnullertTidspunkt() == null
                    && avtale.getAvbruttGrunn() == null
                    && avtale.getGjeldendeInnhold().getSumLonnstilskudd() != null) {

                avtale.reUtregnRedusert();
                avtale.nyeTilskuddsperioderEtterMigreringFraArena(migreringsDato, false);
                avtaleRepository.save(avtale);
                antallUnder67.getAndIncrement();
            }
        });
        log.info("Ferdig kjørt reberegning av fiks for manglende redusert prosent og redusert sum på {} avtaler", antallUnder67);
    }

    @PostMapping("/reberegn-mangler-dato-for-redusert-prosent-dry-run/{migreringsDato}")
    public void reberegnVarigLønnstilskuddSomIkkeHarRedusertDatoDryRun(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        log.info("DRY-RUN: Starter DRY-RUN jobb for å fikse manglende redusert prosent og redusert sum");
        // 1. Generer dato for redusert prosent og sumRedusert
        List<Avtale> varigeLønnstilskudd = avtaleRepository.findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype.VARIG_LONNSTILSKUDD);
        log.info("DRY-RUN: Fant {} varige lønnstilskudd avtaler som mangler redusert prosent til fiksing.", varigeLønnstilskudd.size());
        AtomicInteger antallUnder67 = new AtomicInteger();
        varigeLønnstilskudd.forEach(avtale -> {
            LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
            LocalDate sluttDato = avtale.getGjeldendeInnhold().getSluttDato();

            if (avtale.getGjeldendeInnhold().getLonnstilskuddProsent() > 67
                    && startDato.isBefore(sluttDato.minusMonths(12))
                    && avtale.getAnnullertTidspunkt() == null
                    && avtale.getAvbruttGrunn() == null
                    && avtale.getGjeldendeInnhold().getSumLonnstilskudd() != null) {
                antallUnder67.getAndIncrement();
            }
        });
        log.info("DRY-RUN: Fant {} avtaler som vil bli kjørt fiksing av redusert sum og sats på", antallUnder67.get());
    }

    @PostMapping("/annuller-tilskuddsperiode/{tilskuddsperiodeId}")
    @Transactional
    public void annullerTilskuddsperiode(@PathVariable("tilskuddsperiodeId") UUID id) {
        log.info("Annullerer tilskuddsperiode {}", id);
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
        Avtale avtale = tilskuddPeriode.getAvtale();
        avtale.annullerTilskuddsperiode(tilskuddPeriode);
        tilskuddPeriodeRepository.save(tilskuddPeriode);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/annuller-og-resend-tilskuddsperiode/{tilskuddsperiodeId}")
    @Transactional
    public void annullerOgResendTilskuddsperiode(@PathVariable("tilskuddsperiodeId") UUID id) {
        log.info("Annullerer tilskuddsperiode {} og resender som godkjent", id);
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
        Avtale avtale = tilskuddPeriode.getAvtale();
        avtale.annullerTilskuddsperiode(tilskuddPeriode);
        avtale.lagNyGodkjentTilskuddsperiodeFraAnnullertPeriode(tilskuddPeriode);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/annuller-og-generer-tilskuddsperiode/{tilskuddsperiodeId}")
    @Transactional
    public void annullerOgGenererTilskuddsperiode(@PathVariable("tilskuddsperiodeId") UUID id) {
        log.info("Annullerer tilskuddsperiode {} og genererer ny ubehandlet", id);
        TilskuddPeriode tilskuddPeriode = tilskuddPeriodeRepository.findById(id).orElseThrow(RessursFinnesIkkeException::new);
        Avtale avtale = tilskuddPeriode.getAvtale();
        if (tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT) {
            avtale.annullerTilskuddsperiode(tilskuddPeriode);
        }
        avtale.lagNyTilskuddsperiodeFraAnnullertPeriode(tilskuddPeriode);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/annuller-og-generer-behandlet-i-arena-perioder/{avtaleId}/{dato}")
    @Transactional
    public void annullerOgGenererBehandletIArenaPerioder(@PathVariable("avtaleId") UUID avtaleId, @PathVariable("dato") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dato) {
        log.info("Annullerer tilskuddsperioder med sluttdato før {} på avtale {} og lager nye med status behandlet i arena", dato, avtaleId);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        List<TilskuddPeriode> tilskuddsperioder = tilskuddPeriodeRepository.findAllByAvtaleAndSluttDatoBefore(avtale, dato);
        log.info("Fant {} tilskuddsperioder som skal annulleres og genereres på nytt med behandlet i arena status", tilskuddsperioder.size());

        tilskuddsperioder.stream().toList().forEach(tp -> {
            avtale.annullerTilskuddsperiode(tp);
            avtale.lagNyBehandletIArenaTilskuddsperiodeFraAnnullertPeriode(tp);
        });

        log.info("Avtale {} har nå {} perioder med status behandlet i arena", avtaleId, avtale.getTilskuddPeriode().stream().filter(tp -> tp.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA).count());
        avtaleRepository.save(avtale);
    }

    @PostMapping("/lag-tilskuddsperioder-for-en-avtale/{avtaleId}/{migreringsDato}")
    @Transactional
    public void lagTilskuddsperioderPåEnAvtale(@PathVariable("avtaleId") UUID id, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate migreringsDato) {
        log.info("Lager tilskuddsperioder på en enkelt avtale {} fra dato {}", id, migreringsDato);
        Avtale avtale = avtaleRepository.findById(id)
                .orElseThrow(RessursFinnesIkkeException::new);
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(migreringsDato, false);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/reberegn-ubehandlede-tilskuddsperioder/{avtaleId}")
    @Transactional
    public void reberegnUbehandledeTilskuddsperioder(@PathVariable("avtaleId") UUID avtaleId) {
        log.info("Reberegner ubehandlede tilskuddsperioder for avtale: {}", avtaleId);
        Avtale avtale = avtaleRepository.findById(avtaleId).orElseThrow(RessursFinnesIkkeException::new);
        avtale.reberegnUbehandledeTilskuddsperioder();
        avtaleRepository.save(avtale);
    }

    @PostMapping("/finn-avtaler-med-tilskuddsperioder-feil-datoer")
    public void finnTilskuddsperioderMedFeilDatoer() {
        log.info("Finner avtaler som har tilskuddsperioder med mindre startdato enn en periode med lavere løpenummer");
        List<Avtale> midlertidige = avtaleRepository.findAllByTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        midlertidige.removeIf(a -> a.getGjeldendeInnhold().getAvtaleInngått() == null);
        midlertidige.removeIf(a -> a.getTilskuddPeriode().isEmpty());

        midlertidige.forEach(avtale -> avtale.getTilskuddPeriode().forEach(tp -> {
            if (tp.getLøpenummer() > 1) {
                TilskuddPeriode forrigePeriode = avtale.getTilskuddPeriode().stream().filter(t -> t.getLøpenummer() == tp.getLøpenummer() - 1).toList().stream().findFirst().orElseThrow();
                if (tp.getStartDato().isBefore(forrigePeriode.getStartDato())) {
                    log.warn("Tilskuddsperiode med id {} har startDato før startDatoen til forrige løpenummer!", tp.getId());
                }
            }
        }));
    }

    @PostMapping("/avtale/{id}/sjekk-tilgang")
    public ResponseEntity<String> sjekkTilgang(@PathVariable UUID id, @RequestBody AvtaleAdminSjekkTilgangRequest body) {
        return avtaleRepository.findById(id)
            .map(avtale -> tilgangskontrollService.hentSkrivetilgang(body.veilederAzureOid(), avtale.getDeltakerFnr()))
            .filter(tilgang -> tilgang instanceof Tilgang.Avvis)
            .map(tilgang -> ResponseEntity.ok(
                "[" + ((Tilgang.Avvis) tilgang).tilgangskode() + "] " + ((Tilgang.Avvis) tilgang).melding()
            ))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/avtale/{id}/sjekk-tilgangsattributter")
    public ResponseEntity sjekkTilgangsatrributter(@PathVariable UUID id) {
        Optional<Avtale> avtaleOpt = avtaleRepository.findById(id);

        return avtaleOpt.map(avtale -> {
                Optional<Tilgangsattributter> tilgangsattributter = tilgangskontrollService
                    .hentTilgangsattributter(avtale.getDeltakerFnr());

                Diskresjonskode diskresjonskode = persondataService.hentDiskresjonskode(avtale.getDeltakerFnr());

                return Map.of(
                    "pdlAdressebeskyttelse", diskresjonskode,
                    "kontor", tilgangsattributter.map(Tilgangsattributter::kontor).orElse(""),
                    "skjermet", tilgangsattributter.map(Tilgangsattributter::skjermet).orElse(false),
                    "diskresjonskode", tilgangsattributter.map(t -> t.diskresjonskode().name()).orElse("")
                );
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/avtale/{id}/sjekk-oppfolginsstatus")
    public ResponseEntity<Map<String, Map<String, String>>> sjekkOppfolgingsstatus(@PathVariable UUID id) {
        Optional<Avtale> avtaleOpt = avtaleRepository.findById(id);

        return avtaleOpt.map(avtale -> {
                Oppfølgingsstatus status = veilarboppfolgingService.hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
                return Map.of(
                    "avtaleOppfolginsstatus", Map.of(
                        "formidlingsgruppe", avtale.getFormidlingsgruppe().name(),
                        "kvalifiseringsgruppe", avtale.getKvalifiseringsgruppe().name(),
                        "oppfolginsenhet", avtale.getEnhetOppfolging()
                    ),
                    "veilarbOppfolginsstatus", Map.of(
                        "formidlingsgruppe", status.getFormidlingsgruppe().name(),
                        "kvalifiseringsgruppe", status.getKvalifiseringsgruppe().name(),
                        "oppfolginsenhet", status.getOppfolgingsenhet()
                    )
                );
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/endre-startdato-for-avtale/{id}")
    public void oppdaterStartdatoForAvtale(@PathVariable UUID id, @RequestBody Map<String, Object> parametere) {
        Avtale avtale = avtaleRepository.findById(id).orElseThrow();

        LocalDate startDato = LocalDate.parse((String) parametere.getOrDefault("startDato", null));
        avtale.midlertidigEndreAvtale(Now.instant(), startDato);
        avtaleRepository.save(avtale);
    }

    @PostMapping("/oppdater-tilskuddsperiode-belop-vtao")
    @Transactional
    public void oppdaterTilskuddsperiodeBelopVtao() {
        Set<Integer> kjenteVtaoSatsAar = VTAO_SATS.getSatsePerioder().keySet().stream()
            .map(LocalDate::getYear)
            .collect(Collectors.toSet());
        List<TilskuddPeriode> perioderUtenBelop = tilskuddPeriodeRepository.ubehandledeVtaoTilskuddUtenBelopForAar(
            kjenteVtaoSatsAar
        );

        perioderUtenBelop.forEach(tilskuddPeriode -> {
            tilskuddPeriode.setBeløp(VTAO_SATS.hentGjeldendeSats(tilskuddPeriode.getStartDato()));
        });
        tilskuddPeriodeRepository.saveAll(perioderUtenBelop);
    }

    @GetMapping("/avtale/diskresjonssjekk")
    public Map<String, ?> sjekkDiskresjonskoder(
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "1000") Integer size
    ) {
        Page<Fnr> pagable = avtaleRepository
            .findDistinctDeltakerFnr(PageRequest.of(Math.abs(page), Math.abs(size)));

        Map<Fnr, Diskresjonskode> diskresjonskodeMap = persondataService
            .hentDiskresjonskoder(new HashSet<>(pagable.getContent()));

        List<Map<String, ?>> avtalerMedDiskresjon = pagable
            .getContent()
            .stream()
            .filter(fnr -> diskresjonskodeMap.getOrDefault(fnr, Diskresjonskode.UGRADERT).erKode6Eller7())
            .flatMap(fnr -> avtaleRepository.findByDeltakerFnr(fnr).stream().map(avtale -> Map.of(
                "id", avtale.getId(),
                "status", avtale.getStatus(),
                "gradering", diskresjonskodeMap.get(fnr)
            )))
            .collect(Collectors.toList());

        return Map.of(
            "antallSider", pagable.getTotalPages(),
            "gjeldendeSide", pagable.getNumber(),
            "avtaler", avtalerMedDiskresjon
        );
    }

    @PostMapping("/oppdaterte-avtalekrav")
    public void oppdaterteAvtalekrav(@RequestBody AvtaleKravRequest avtaleKravRequest) {
        adminService.oppdaterteAvtalekrav(avtaleKravRequest.avtaleKravTidspunkt());
    }

}
