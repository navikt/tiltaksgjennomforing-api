package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndreAvtaleArena {
    private LocalDate startDato;
    private LocalDate sluttDato;
    private Integer stillingprosent;
    private Integer antallDagerPerUke;
}
