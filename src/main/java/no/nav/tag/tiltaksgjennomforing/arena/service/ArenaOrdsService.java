package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.client.ords.ArenaOrdsArbeidsgiverResponse;
import no.nav.tag.tiltaksgjennomforing.arena.client.ords.ArenaOrdsClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.ords.ArenaOrdsFnrResponse;
import no.nav.tag.tiltaksgjennomforing.arena.models.ords.ArenaOrdsArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.arena.models.ords.ArenaOrdsFnr;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaOrdsArbeidsgiverRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaOrdsFnrRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ArenaOrdsService {
    private final ArenaOrdsFnrRepository fnrRepository;
    private final ArenaOrdsArbeidsgiverRepository arbeidsgiverRepository;
    private final ArenaOrdsClient arenaOrdsClient;

    public ArenaOrdsService(
        ArenaOrdsFnrRepository fnrRepository,
        ArenaOrdsArbeidsgiverRepository arbeidsgiverRepository,
        ArenaOrdsClient arenaOrdsClient
    ) {
        this.fnrRepository = fnrRepository;
        this.arbeidsgiverRepository = arbeidsgiverRepository;
        this.arenaOrdsClient = arenaOrdsClient;
    }

    public void fetchPerson(Integer personId) {
        if (personId == null) {
            log.info("PersonId er null - henter ikke fnr fra ORDS");
            return;
        }

        boolean deltakerFnrExist = fnrRepository.existsById(personId);
        log.info("Henter fnr med id {}", personId);

        if (deltakerFnrExist) {
            log.info("Fnr med id {} finnes allerede", personId);
            return;
        }

        Optional<ArenaOrdsFnrResponse> responseOpt = arenaOrdsClient.getFnr(personId);
        if (responseOpt.isEmpty()) {
            log.info("Fant ikke fnr i ORDS for person med id {}", personId);
            return;
        }

        ArenaOrdsFnrResponse response = responseOpt.get();
        ArenaOrdsFnr arenaOrdsFnr = ArenaOrdsFnr.builder()
            .personId(personId)
            .fnr(response.personListe().getFirst().fnr())
            .build();

        fnrRepository.save(arenaOrdsFnr);
    }

    public void attemptDeleteFnr(Integer personId) {
        if (personId == null) {
            return;
        }

        try {
            log.info("Sletter fnr med id {}", personId);
            fnrRepository.deleteById(personId);
        } catch (DataIntegrityViolationException e) {
            log.info("Person {} er fortsatt er i bruk", personId);
        }
    }

    public void fetchArbeidsgiver(Integer arbeidsgiverId) {
        if (arbeidsgiverId == null) {
            log.info("ArbeidsgiverId er null - henter ikke fnr fra ORDS");
            return;
        }

        boolean arbeidsgiverExist = arbeidsgiverRepository.existsById(arbeidsgiverId);
        log.info("Henter arbeidsgiver med id {}", arbeidsgiverId);

        if (arbeidsgiverExist) {
            log.info("Arbeidsgiver med id {} finnes allerede", arbeidsgiverId);
            return;
        }

        Optional<ArenaOrdsArbeidsgiverResponse> responseOpt = arenaOrdsClient.getArbeidsgiver(arbeidsgiverId);
        if (responseOpt.isEmpty()) {
            log.info("Fant ikke virksomhetsnummer i ORDS for arbeidsgiver med id {}", arbeidsgiverId);
            return;
        }

        ArenaOrdsArbeidsgiverResponse response = responseOpt.get();
        ArenaOrdsArbeidsgiver arenaOrdsArbeidsgiver = ArenaOrdsArbeidsgiver.builder()
            .arbgivIdArrangor(arbeidsgiverId)
            .virksomhetsnummer(response.bedriftsnr().toString())
            .organisasjonsnummerMorselskap(response.orgnrMorselskap().toString())
            .build();

        arbeidsgiverRepository.save(arenaOrdsArbeidsgiver);
    }

    public void attemptDeleteArbeidsgiver(Integer arbeidsgiverId) {
        if (arbeidsgiverId == null) {
            return;
        }

        try {
            log.info("Sletter arbeidsgiver med id {}", arbeidsgiverId);
            arbeidsgiverRepository.deleteById(arbeidsgiverId);
        } catch (DataIntegrityViolationException e) {
            log.info("Arbeidsgiver {} er fortsatt er i bruk", arbeidsgiverId);
        }
    }
}
