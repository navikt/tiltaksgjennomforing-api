package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.oppfolging.Oppfolging;

import java.time.LocalDate;

public record KommendeOppfolgingDTO(
    boolean oppfolgingKanUtfores,
    LocalDate oppfolgingstarter,
    LocalDate oppfolgingsfrist
) {
    public static KommendeOppfolgingDTO fraAvtale(Avtale avtale) {
        var oppfolging = Oppfolging.fra(avtale);
        if (oppfolging.gyldigDatoForOppfolging()) {
            return new KommendeOppfolgingDTO(
                avtale.getOppfolgingVarselSendt() != null,
                oppfolging.getVarselstidspunkt(),
                oppfolging.getOppfolgingsfrist()
            );
        }
        return null;
    }
}
