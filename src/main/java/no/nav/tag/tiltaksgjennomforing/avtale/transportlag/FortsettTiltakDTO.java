package no.nav.tag.tiltaksgjennomforing.avtale.transportlag;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.oppfolging.Oppfolging;

import java.time.LocalDate;

public record FortsettTiltakDTO(
    boolean oppfolgingKanUtfores,
    LocalDate oppfolgingstarter,
    LocalDate oppfolgingsfrist
) {
    public static FortsettTiltakDTO fraAvtale(Avtale avtale) {
        var oppfolging = Oppfolging.fra(avtale);
        if (oppfolging.gyldigDatoForOppfolging()) {
            return new FortsettTiltakDTO(
                avtale.getOppfolgingVarselSendt() != null,
                oppfolging.getVarselstidspunkt(),
                oppfolging.getOppfolgingsfrist()
            );
        }
        return null;
    }
}
