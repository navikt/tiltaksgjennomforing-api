package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNrConverter;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.springframework.data.domain.AbstractAggregateRoot;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class ArbeidsgiverNotifikasjon extends AbstractAggregateRoot<ArbeidsgiverNotifikasjon> {

    @Id
    private UUID id;
    private LocalDateTime tidspunkt;
    private UUID avtaleId;
    @Enumerated(EnumType.STRING)
    private VarslbarHendelseType hendelseType;
    @Convert(converter = BedriftNrConverter.class)
    private BedriftNr virksomhetsnummer;
    private String lenke;
    private Integer serviceCode;
    private Integer serviceEdition;
    private boolean hendelseUtfort;
    private String status;

    public static ArbeidsgiverNotifikasjon nyHendelse(
            Avtale avtale,
            VarslbarHendelseType varslbarHendelseType,
            NotifikasjonService notifikasjonMSAService,
            NotifikasjonParser notifikasjonParser) {

        final AltinnNotifikasjonsProperties notifikasjonerProperties =
                notifikasjonParser.getNotifikasjonerProperties(avtale);

        final String lenke =
                notifikasjonMSAService.getAvtaleLenke(avtale);

        ArbeidsgiverNotifikasjon notifikasjon = new ArbeidsgiverNotifikasjon();
        notifikasjon.id = UUID.randomUUID();
        notifikasjon.tidspunkt = LocalDateTime.now();
        notifikasjon.hendelseType = varslbarHendelseType;
        notifikasjon.virksomhetsnummer = avtale.getBedriftNr();
        notifikasjon.lenke = lenke;
        notifikasjon.serviceCode = notifikasjonerProperties.getServiceCode();
        notifikasjon.serviceEdition = notifikasjonerProperties.getServiceEdition();
        notifikasjon.hendelseUtfort = false;

        return notifikasjon;
    }
}
