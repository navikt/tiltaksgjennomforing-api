package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;

public record Gjeldende14aVedtakRespons(
    Innsatsgruppe innsatsgruppe,
    Hovedmal hovedmal,
    ZonedDateTime fattetDato
) {
    public enum Hovedmal {
        SKAFFE_ARBEID,
        BEHOLDE_ARBEID,
        OKE_DELTAKELSE
    }

    @Override
    public @NonNull String toString() {
        return "{ innsatsgruppe: " + innsatsgruppe + ", hovedmal: " + hovedmal + ", fattetDato: " + fattetDato + " }";
    }
}
