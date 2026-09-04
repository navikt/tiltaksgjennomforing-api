package no.nav.tag.tiltaksgjennomforing.avtale;


import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class FilterSok {
    @Id
    private String sokId;
    private Instant sistSoktTidspunkt;
    private String queryParametre;
    private Integer antallGangerSokt;

    @SneakyThrows
    public FilterSok(AvtaleQueryParameter queryParametre) {
        this.sistSoktTidspunkt = Now.instant();
        this.antallGangerSokt = 1;
        this.sokId = queryParametre.generateHash();
        ObjectMapper mapper = new ObjectMapper();
        this.queryParametre = mapper.writeValueAsString(queryParametre);
    }

    public boolean erLik(AvtaleQueryParameter avtalePredicate) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(this.queryParametre, AvtaleQueryParameter.class).equals(avtalePredicate);
        } catch (JacksonException e) {
            return false;
        }
    }

    public AvtaleQueryParameter getAvtalePredicate() {
        ObjectMapper mapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        try {
            return mapper.readValue(this.queryParametre, AvtaleQueryParameter.class);
        } catch (JacksonException e) {
            return new AvtaleQueryParameter();
        }
    }
}
