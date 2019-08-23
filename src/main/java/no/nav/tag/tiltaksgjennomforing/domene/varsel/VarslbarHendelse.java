package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.IdOgTidspunktGenerator;
import no.nav.tag.tiltaksgjennomforing.domene.events.VarslbarHendelseOppstaatt;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class VarslbarHendelse extends AbstractAggregateRoot implements IdOgTidspunktGenerator {
    @Id
    private UUID id;
    private LocalDateTime tidspunkt;
    private UUID avtaleId;
    private VarslbarHendelseType varslbarHendelseType;
    @Column(keyColumn = "id")
    private List<SmsVarsel> smsVarsler = new ArrayList<>();

    public static VarslbarHendelse nyHendelse(Avtale avtale, VarslbarHendelseType varslbarHendelseType) {
        VarslbarHendelse varslbarHendelse = new VarslbarHendelse();
        varslbarHendelse.avtaleId = avtale.getId();
        varslbarHendelse.varslbarHendelseType = varslbarHendelseType;
        varslbarHendelse.smsVarsler = varslbarHendelse.lagSmsVarsler(avtale);
        varslbarHendelse.registerEvent(new VarslbarHendelseOppstaatt(avtale, varslbarHendelse));
        return varslbarHendelse;
    }

    private List<SmsVarsel> lagSmsVarsler(Avtale avtale) {
        SmsVarselFactory factory = new SmsVarselFactory(avtale);
        switch (varslbarHendelseType) {
            case OPPRETTET:
                break;
            case GODKJENT_AV_DELTAKER:
            case GODKJENT_AV_ARBEIDSGIVER:
                return Arrays.asList(factory.veileder());
            case GODKJENT_AV_VEILEDER:
                return Arrays.asList(factory.deltaker(), factory.arbeidsgiver());
            case GODKJENT_PAA_VEGNE_AV:
                return Arrays.asList(factory.arbeidsgiver());
            case GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER: {
                var varslinger = new ArrayList<SmsVarsel>();
                if (avtale.erGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                varslinger.add(factory.veileder());
                return varslinger;
            }
            case GODKJENNINGER_OPPHEVET_AV_VEILEDER: {
                var varslinger = new ArrayList<SmsVarsel>();
                if (avtale.erGodkjentAvDeltaker()) {
                    varslinger.add(factory.deltaker());
                }
                if (avtale.erGodkjentAvArbeidsgiver()) {
                    varslinger.add(factory.arbeidsgiver());
                }
                return varslinger;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void settIdOgOpprettetTidspunkt() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (tidspunkt == null) {
            tidspunkt = LocalDateTime.now();
        }
        this.smsVarsler.forEach(SmsVarsel::settId);
    }
}
