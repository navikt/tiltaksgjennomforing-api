package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SmsVarselMeldingMapper {
    @Mapping(target = "smsVarselId", source = "id")
    SmsVarselMelding tilMelding(SmsVarsel smsVarsel);
}