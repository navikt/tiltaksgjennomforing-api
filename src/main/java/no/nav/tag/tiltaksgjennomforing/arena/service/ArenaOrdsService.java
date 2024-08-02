package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.client.ArenaOrdsArbeidsgiverResponse;
import no.nav.tag.tiltaksgjennomforing.arena.client.ArenaOrdsClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.ArenaOrdsFnrResponse;
import no.nav.tag.tiltaksgjennomforing.arena.models.ords.ArenaOrdsArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.arena.models.ords.ArenaOrdsFnr;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaOrdsArbeidsgiverRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaOrdsFnrRepository;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArenaOrdsService {
    private final ArenaOrdsFnrRepository fnrRepository;
    private final ArenaOrdsArbeidsgiverRepository arbeidsgiverRepository;
    private final ArenaOrdsClient arenaOrdsClient;
    private final Environment env;

    public ArenaOrdsService(
        ArenaOrdsFnrRepository fnrRepository,
        ArenaOrdsArbeidsgiverRepository arbeidsgiverRepository,
        ArenaOrdsClient arenaOrdsClient,
        Environment env
    ) {
        this.fnrRepository = fnrRepository;
        this.arbeidsgiverRepository = arbeidsgiverRepository;
        this.arenaOrdsClient = arenaOrdsClient;
        this.env = env;
    }

    public void fetchPerson(int personId) {
        boolean deltakerFnrExist = fnrRepository.existsById(personId);
        log.info("Henter fnr med id {}", personId);

        if (deltakerFnrExist) {
            log.info("Fnr med id {} finnes allerede", personId);
            return;
        }

        if (env.matchesProfiles(Miljø.DEV_FSS)) {
            log.info("Mocker kall til ORDS i dev-fss");

            fnrRepository.save(
                ArenaOrdsFnr.builder()
                    .personId(personId)
                    .fnr("12345678901")
                    .build()
            );
            return;
        }

        ArenaOrdsFnrResponse response = arenaOrdsClient.getFnr(personId);

        ArenaOrdsFnr arenaOrdsFnr = ArenaOrdsFnr.builder()
            .personId(personId)
            .fnr(response.personListe().getFirst().fnr())
            .build();

        fnrRepository.save(arenaOrdsFnr);
    }

    public void attemptDeleteFnr(int personId) {
        try {
            log.info("Sletter fnr med id {}", personId);
            fnrRepository.deleteById(personId);
        } catch (DataIntegrityViolationException e) {
            log.info("Person {} er fortsatt er i bruk", personId);
        }
    }

    public void fetchArbeidsgiver(int arbeidsgiverId) {
        boolean arbeidsgiverExist = arbeidsgiverRepository.existsById(arbeidsgiverId);
        log.info("Henter arbeidsgiver med id {}", arbeidsgiverId);

        if (arbeidsgiverExist) {
            log.info("Arbeidsgiver med id {} finnes allerede", arbeidsgiverId);
            return;
        }

        if (env.matchesProfiles(Miljø.DEV_FSS)) {
            log.info("Mocker kall til ORDS i dev-fss");

            arbeidsgiverRepository.save(
                ArenaOrdsArbeidsgiver.builder()
                    .arbgivIdArrangor(arbeidsgiverId)
                    .virksomhetsnummer("123456789")
                    .organisasjonsnummerMorselskap("123456789")
                    .build()
            );
            return;
        }

        ArenaOrdsArbeidsgiverResponse response = arenaOrdsClient.getArbeidsgiver(arbeidsgiverId);

        ArenaOrdsArbeidsgiver arenaOrdsArbeidsgiver = ArenaOrdsArbeidsgiver.builder()
            .arbgivIdArrangor(arbeidsgiverId)
            .virksomhetsnummer(response.bedriftsnr().toString())
            .organisasjonsnummerMorselskap(response.orgnrMorselskap().toString())
            .build();

        arbeidsgiverRepository.save(arenaOrdsArbeidsgiver);
    }

    public void attemptDeleteArbeidsgiver(int arbeidsgiverId) {
        try {
            log.info("Sletter arbeidsgiver med id {}", arbeidsgiverId);
            arbeidsgiverRepository.deleteById(arbeidsgiverId);
        } catch (DataIntegrityViolationException e) {
            log.info("Arbeidsgiver {} er fortsatt er i bruk", arbeidsgiverId);
        }
    }
}
