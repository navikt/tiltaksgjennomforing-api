package no.nav.tag.tiltaksgjennomforing.avtale;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class FilterSok {
    @Id
    private String sokId;
    private LocalDateTime tidspunktSokt;
    private String queryParametre;
    private Integer antallGangerSokt;


    @SneakyThrows
    public FilterSok(AvtalePredicate queryParametre) {
        this.tidspunktSokt = LocalDateTime.now();
        this.antallGangerSokt = 1;
        this.sokId = queryParametre.generateHash();
        ObjectMapper mapper = new ObjectMapper();
        this.queryParametre = mapper.writeValueAsString(queryParametre);
    }

    public boolean erLik(AvtalePredicate avtalePredicate) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(this.queryParametre, AvtalePredicate.class).equals(avtalePredicate);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public AvtalePredicate getAvtalePredicate() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(this.queryParametre, AvtalePredicate.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
