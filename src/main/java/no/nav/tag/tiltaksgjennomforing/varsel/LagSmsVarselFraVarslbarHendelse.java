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
public class LagSmsVarselFraVarslbarHendelse {
    private final SmsVarselRepository smsVarselRepository;

    static List<SmsVarsel> lagSmsVarsler(Avtale avtale, VarslbarHendelse hendelse, GamleVerdier gamleVerdier) {
        SmsVarselFactory factory = new SmsVarselFactory(avtale, hendelse);
        switch (hendelse.getVarslbarHendelseType()) {
            case GODKJENT_AV_DELTAKER:
            case GODKJENT_AV_ARBEIDSGIVER:
                return List.of(factory.veileder());
            case AVTALE_INNGÅTT:
                return List.of(factory.deltaker(), factory.arbeidsgiver());
            case DELT_MED_ARBEIDSGIVER:
                return List.of(factory.arbeidsgiver());
            case REFUSJON_KLAR:
                return List.of(factory.arbeidsgiverRefusjonKlar());
            case REFUSJON_KLAR_REVARSEL:
                return List.of(factory.arbeidsgiverRefusjonKlarRevarsel());
            case GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER: {
                var varslinger = new ArrayList<SmsVarsel>();
                if (gamleVerdier.isGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                varslinger.add(factory.veileder());
                return varslinger;
            }
            case GODKJENNINGER_OPPHEVET_AV_VEILEDER: {
                var varslinger = new ArrayList<SmsVarsel>();
                if (gamleVerdier.isGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                if (gamleVerdier.isGodkjentAvArbeidsgiver()) {
                    varslinger.add(factory.arbeidsgiver());
                }
                return varslinger;
            }
            case DELT_MED_DELTAKER:
                return List.of(factory.deltaker());

        }
        return Collections.emptyList();
    }

    @EventListener
    public void lagreSmsVarsler(VarslbarHendelseOppstaatt event) {
        smsVarselRepository.saveAll(lagSmsVarsler(event.getAvtale(), event.getVarslbarHendelse(), event.getGamleVerdier()));
    }
}
