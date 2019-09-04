package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import no.nav.tag.tiltaksgjennomforing.domene.varsel.SmsVarsel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SmsVarselMeldingMapper {
    @Mapping(target = "smsVarselId", source = "id")
    SmsVarselMelding tilMelding(SmsVarsel smsVarsel);
}