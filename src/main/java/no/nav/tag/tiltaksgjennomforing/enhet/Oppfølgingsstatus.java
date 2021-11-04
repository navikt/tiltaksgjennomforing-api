package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Oppfølgingsstatus {
    public String formidlingsgruppe;
    @Enumerated(EnumType.STRING)
    public Kvalifiseringsgruppe kvalifiseringsgruppe;
    public String oppfolgingsenhet;
}
