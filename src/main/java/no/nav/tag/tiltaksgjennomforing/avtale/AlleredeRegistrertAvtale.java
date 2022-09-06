package no.nav.tag.tiltaksgjennomforing.avtale;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlleredeRegistrertAvtale {

    private UUID id;
    private Integer avtaleNr;
    private Tiltakstype tiltakstype;
    private Fnr deltakerFnr;
    private BedriftNr bedriftNr;
    private NavIdent veilederNavIdent;

    private LocalDate startDato;
    private LocalDate sluttDato;
    private LocalDateTime godkjentAvVeileder;
    private LocalDateTime godkjentAvBeslutter;
    private LocalDateTime avtaleInngått;

}
