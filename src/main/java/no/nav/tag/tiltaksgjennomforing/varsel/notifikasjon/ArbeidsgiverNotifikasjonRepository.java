package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import io.micrometer.core.annotation.Timed;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ArbeidsgiverNotifikasjonRepository extends JpaRepository<ArbeidsgiverNotifikasjon, UUID> {

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<ArbeidsgiverNotifikasjon> findArbeidsgiverNotifikasjonByAvtaleIdAndVarselSendtVellykketAndNotifikasjonAktiv(UUID id, boolean varselSendtVellykket, boolean notifikasjonAktiv);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<ArbeidsgiverNotifikasjon> findAllByAvtaleId(UUID id);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<ArbeidsgiverNotifikasjon> findArbeidsgiverNotifikasjonByAvtaleIdAndHendelseTypeAndStatusResponse(UUID id, VarslbarHendelseType hendelsetype, String statusResponse);

}
