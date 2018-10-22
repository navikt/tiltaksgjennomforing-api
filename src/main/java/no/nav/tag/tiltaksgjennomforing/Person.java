package no.nav.tag.tiltaksgjennomforing;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Person {
    @Id
    private Integer id;
    private String fornavn;
    private String etternavn;
}
