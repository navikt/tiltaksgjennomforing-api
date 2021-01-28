package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GamleVerdier;
import no.nav.tag.tiltaksgjennomforing.varsel.events.VarslbarHendelseOppstaatt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LagBjelleVarselFraVarslbarHendelse {
    private final BjelleVarselRepository bjelleVarselRepository;

    static List<BjelleVarsel> lagBjelleVarsler(Avtale avtale, VarslbarHendelse varslbarHendelse, GamleVerdier gamleVerdier) {
        var factory = new BjelleVarselFactory(avtale, varslbarHendelse);
        switch (varslbarHendelse.getVarslbarHendelseType()) {
            case OPPRETTET:
            case GODKJENT_AV_VEILEDER:
                return List.of(factory.deltaker(), factory.arbeidsgiver());
            case GODKJENT_AV_DELTAKER:
            case GODKJENT_AV_ARBEIDSGIVER:
            case TILSKUDDSPERIODE_AVSLATT:
                return List.of(factory.veileder());
            case GODKJENT_PAA_VEGNE_AV:
                return List.of(factory.arbeidsgiver());
            case GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER: {
                var varslinger = new ArrayList<BjelleVarsel>();
                if (gamleVerdier.isGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                varslinger.add(factory.veileder());
                return varslinger;
            }
            case GODKJENNINGER_OPPHEVET_AV_VEILEDER: {
                var varslinger = new ArrayList<BjelleVarsel>();
                if (gamleVerdier.isGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                if (gamleVerdier.isGodkjentAvArbeidsgiver()) {
                    varslinger.add(factory.arbeidsgiver());
                }
                return varslinger;
            }
        }
        return Collections.emptyList();
    }

    @EventListener
    public void lagreBjelleVarsler(VarslbarHendelseOppstaatt event) {
        bjelleVarselRepository.saveAll(lagBjelleVarsler(event.getAvtale(), event.getVarslbarHendelse(), event.getGamleVerdier()));
    }
}
