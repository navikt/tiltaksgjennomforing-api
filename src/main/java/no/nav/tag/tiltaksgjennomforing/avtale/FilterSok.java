package no.nav.tag.tiltaksgjennomforing.avtale;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class FilterSok {
    @Id
    private String sokId;
    private LocalDateTime sistSoktTidspunkt;
    private String queryParametre;
    private Integer antallGangerSokt;


    @SneakyThrows
    public FilterSok(AvtaleQueryParameter queryParametre) {
        this.sistSoktTidspunkt = Now.localDateTime();
        this.antallGangerSokt = 1;
        this.sokId = queryParametre.generateHash();
        ObjectMapper mapper = new ObjectMapper();
        this.queryParametre = mapper.writeValueAsString(queryParametre);
    }

    public boolean erLik(AvtaleQueryParameter avtalePredicate) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(this.queryParametre, AvtaleQueryParameter.class).equals(avtalePredicate);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public AvtaleQueryParameter getAvtalePredicate() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(this.queryParametre, AvtaleQueryParameter.class);
        } catch (JsonProcessingException e) {
            return new AvtaleQueryParameter();
        }
    }
}
