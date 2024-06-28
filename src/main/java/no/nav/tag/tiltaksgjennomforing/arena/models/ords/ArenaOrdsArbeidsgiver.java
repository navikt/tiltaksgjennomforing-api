package no.nav.tag.tiltaksgjennomforing.arena.models.ords;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArenaOrdsArbeidsgiver {
    @Id
    private int arbgivIdArrangor;
    private String virksomhetsnummer;
    private String organisasjonsnummerMorselskap;
}
